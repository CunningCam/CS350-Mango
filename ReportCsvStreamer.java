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
package com.serotonin.mango.vo.report;

import java.io.PrintWriter;
import java.util.ResourceBundle;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.serotonin.mango.view.export.CsvWriter;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.web.i18n.I18NUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles streaming data for reports in CSV format, allowing dynamic
 * inclusion of headers based on configuration.
 */
public class ReportCsvStreamer implements ReportDataStreamHandler {
    private final PrintWriter out;
    private TextRenderer textRenderer;
    private final String[] data = new String[5];
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
    private final CsvWriter csvWriter = new CsvWriter();
    private static final Logger logger = Logger.getLogger(ReportCsvStreamer.class.getName());

    /**
     * Constructor for ReportCsvStreamer.
     * @param out The PrintWriter object to which the CSV data is written.
     * @param bundle The resource bundle used to localize headers.
     * @param writeHeaders If true, headers are written at the beginning of the CSV output.
     */
    public ReportCsvStreamer(PrintWriter out, ResourceBundle bundle, boolean writeHeaders) {
        this.out = out;

        // Initialize CSV headers if required
        if (writeHeaders) {
            writeHeaders(bundle);
        }
    }

    /**
     * Writes headers to the CSV file using resource bundle for localization.
     * @param bundle The resource bundle containing localized strings.
     */
    private void writeHeaders(ResourceBundle bundle) {
        try {
            data[0] = I18NUtils.getMessage(bundle, "reports.pointName");
            data[1] = I18NUtils.getMessage(bundle, "common.time");
            data[2] = I18NUtils.getMessage(bundle, "common.value");
            data[3] = I18NUtils.getMessage(bundle, "reports.rendered");
            data[4] = I18NUtils.getMessage(bundle, "common.annotation");
            out.write(csvWriter.encodeRow(data));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error writing headers to CSV", e);
        }
    }

    /**
     * Begins processing a new report point, setting up necessary text rendering.
     * @param pointInfo Information about the report point to process.
     */
    public void startPoint(ReportPointInfo pointInfo) {
        data[0] = pointInfo.getExtendedName();
        textRenderer = pointInfo.getTextRenderer();
    }

    /**
     * Writes data for a single report point to the CSV file.
     * @param rdv The data value object containing information to be written.
     */
    public void pointData(ReportDataValue rdv) {
        try {
            data[1] = dtf.print(new DateTime(rdv.getTime()));
            if (rdv.getValue() == null) {
                data[2] = data[3] = null;
            } else {
                data[2] = rdv.getValue().toString();
                data[3] = textRenderer.getText(rdv.getValue(), TextRenderer.HINT_FULL);
            }
            data[4] = rdv.getAnnotation();
            out.write(csvWriter.encodeRow(data));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing data point", e);
        }
    }

    /**
     * Finalizes the CSV output and closes the print writer.
     */
    public void done() {
        try {
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error closing stream", e);
        }
    }
}
