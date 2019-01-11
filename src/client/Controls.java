/**
 * Copyright (C) 2019 codedbymorituri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package client;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class Controls extends JPanel {

    private JLabel dragAndDropLabel;
    private JCheckBox includeTrackersCheckBox;
    private JButton convertButton;
    private GridBagConstraints c;

    private File torrentFile;

    public Controls() {
        initLayout();
        initComponents();
        initDragAndDrop();
        initListeners();
    }

    private void initLayout() {
        setLayout(new GridBagLayout());
    }

    private void initComponents() {
        c = new GridBagConstraints();
        addDragAndDropLabel();
        addIncludeTrackersCheckBox();
        addConvertButton();
    }

    private void addDragAndDropLabel() {
        dragAndDropLabel = new JLabel();
        dragAndDropLabel.setText("Drag and Drop .torrent file here");
        dragAndDropLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dragAndDropLabel.setVerticalAlignment(SwingConstants.CENTER);
        dragAndDropLabel.setPreferredSize(new Dimension(320, 80));
        Border outerBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        Border innerBorder = BorderFactory.createLineBorder(Color.lightGray);
        dragAndDropLabel.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        add(dragAndDropLabel, c);
    }

    private void addIncludeTrackersCheckBox() {
        includeTrackersCheckBox = new JCheckBox();
        includeTrackersCheckBox.setText("Include trackers");
        includeTrackersCheckBox.setSelected(false);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        add(includeTrackersCheckBox, c);
    }

    private void addConvertButton() {
        convertButton = new JButton();
        convertButton.setText("Convert");
        c.anchor = GridBagConstraints.LINE_END;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 4, 4);
        add(convertButton, c);
    }

    private void initDragAndDrop() {
        dragAndDropLabel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> droppedFiles = (List<File>) e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File fileDropped : droppedFiles) {
                        if (checkExtensionValid(fileDropped)) {
                            torrentFile = fileDropped;
                            dragAndDropLabel.setText("<html><center>" + fileDropped.getName() + "</center></html>");
                            break;
                        }
                    }
                } catch (Exception ex) {
                    //System.out.println(ex.getMessage());
                }
            }
        });
    }

    private boolean checkExtensionValid(File fileDropped) {
        String fileExtension = getFileExtension(fileDropped);
        return ("torrent".equals(fileExtension.toLowerCase()));
    }

    private String getFileExtension(File fileDropped) {
        String fileName = fileDropped.getName();
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index + 1);
     }

    private void initListeners() {
        convertButton.addActionListener(convertButtonClicked);
    }

    private ActionListener convertButtonClicked = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (torrentFile != null) {
                Data.controller.convert(torrentFile, includeTrackersCheckBox.isSelected());
            }
        }
    };
}
