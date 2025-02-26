/*
 * Part of Simbrain--a java-based neural network kit Copyright (C) 2005,2007 The
 * Authors. See http://www.simbrain.net/credits This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.simbrain.plot.projection;

import org.simbrain.util.ResourceManager;
import org.simbrain.util.SFileChooser;
import org.simbrain.util.projection.Projector;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Actions for projection plot.
 *
 * @author jyoshimi
 */
public class ProjectionPlotActions {

    /**
     * Default directory where csv files are stored.
     */
    private static String CSV_DIRECTORY = "." + System.getProperty("file.separator") + "simulations" + System.getProperty("file.separator") + "tables";

    /**
     * Export high dimensional data to .csv.
     */
    public static Action getExportDataHi(final Projector proj) {
        return new AbstractAction() {

            // Initialize
            {
                putValue(SMALL_ICON, ResourceManager.getImageIcon("menu_icons/Save.png"));
                putValue(NAME, "Export hi-d (.csv)");
                putValue(SHORT_DESCRIPTION, "Export hi-d data (.csv)");
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SFileChooser chooser = new SFileChooser(CSV_DIRECTORY, "comma-separated-values (csv)", "csv");
                File theFile = chooser.showSaveDialog();
                if (theFile != null) {
                    proj.getUpstairs().exportToCSV(theFile);
                }

            }
        };
    }

    /**
     * Export low dimensional data to .csv.
     */
    public static Action getExportDataLow(final Projector proj) {
        return new AbstractAction() {

            // Initialize
            {
                putValue(SMALL_ICON, ResourceManager.getImageIcon("menu_icons/Save.png"));
                putValue(NAME, "Export low-d (.csv)");
                putValue(SHORT_DESCRIPTION, "Export low-d data (.csv)");
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SFileChooser chooser = new SFileChooser(CSV_DIRECTORY, "comma-separated-values (csv)", "csv");
                File theFile = chooser.showSaveDialog();
                if (theFile != null) {
                    proj.getDownstairs().exportToCSV(theFile);
                }

            }
        };
    }

    /**
     * Import high dimensional data from .csv.
     *
     * @return the action the action
     */
    public static Action getImportData(final Projector proj) {
        return new AbstractAction() {

            // Initialize
            {
                putValue(SMALL_ICON, ResourceManager.getImageIcon("menu_icons/Open.png"));
                putValue(NAME, "Import data (.csv)");
                putValue(SHORT_DESCRIPTION, "Import data (.csv)");
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SFileChooser chooser = new SFileChooser(CSV_DIRECTORY, "comma-separated-values (csv)", "csv");
                File theFile = chooser.showOpenDialog();
                if (theFile != null) {
                    proj.importData(theFile);
                }

            }
        };
    }

}
