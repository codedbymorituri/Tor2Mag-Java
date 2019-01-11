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
import java.awt.*;
import java.io.*;

public class Client extends JFrame {

    private Controls controls;
    private GridBagConstraints c;

    public Client() {
        Data.controller = this;
        initLayout();
        initComponents();
    }

    private void initLayout() {
        setTitle("Tor2Mag " + Data.version);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon-128.png")));
        setResizable(false);
        setLayout(new GridBagLayout());
    }

    private void initComponents() {
        c = new GridBagConstraints();
        addControls();
        pack();
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addControls() {
        controls = new Controls();
        c.gridx = 0;
        c.gridy = 0;
        add(controls, c);
    }

    public void convert(File torrentFile, Boolean includeTrackers) {
        Converter converter = new Converter();
        converter.convert(torrentFile, includeTrackers);
    }

}
