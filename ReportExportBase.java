/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.web.servlet;

import java.io.IOException;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.ReportDao;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.vo.report.ReportInstance;
import com.serotonin.mango.vo.report.ReportCsvStreamer; // Ensure this class is correctly defined in your project

public class ReportExportBase extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final int CONTENT_REPORT = 1;
    private static final int CONTENT_EVENTS = 2;
    private static final int CONTENT_COMMENTS = 3;

    protected void execute(HttpServletRequest request, HttpServletResponse response, int content) throws ServletException {
        try {
            int instanceId = Integer.parseInt(request.getParameter("instanceId"));
            ReportDao reportDao = new ReportDao();
            ReportInstance instance = reportDao.getReportInstance(instanceId);

            Permissions.ensureReportInstancePermission(Common.getUser(request), instance);

            response.setContentType("text/csv");
            ResourceBundle bundle = Common.getBundle();

            switch (content) {
                case CONTENT_REPORT:
                    ReportCsvStreamer reportCreator = new ReportCsvStreamer(response.getWriter(), bundle, true);
                    reportDao.reportInstanceData(instanceId, reportCreator);
                    break;
                case CONTENT_EVENTS:
                    // Assuming EventCsvStreamer is properly defined elsewhere
                    EventCsvStreamer eventCreator = new EventCsvStreamer(response.getWriter(), reportDao.getReportInstanceEvents(instanceId), bundle);
                    eventCreator.streamData(); // Make sure this method exists in EventCsvStreamer
                    break;
                case CONTENT_COMMENTS:
                    // Assuming UserCommentCsvStreamer is properly defined elsewhere
                    UserCommentCsvStreamer commentCreator = new UserCommentCsvStreamer(response.getWriter(), reportDao.getReportInstanceUserComments(instanceId), bundle);
                    commentCreator.streamData(); // Make sure this method exists in UserCommentCsvStreamer
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported content type: " + content);
            }
        } catch (NumberFormatException e) {
            throw new ServletException("Invalid format for instance ID", e);
        } catch (Exception e) {
            throw new ServletException("Error processing report export", e);
        }
    }
}
