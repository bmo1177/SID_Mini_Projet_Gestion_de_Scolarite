/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestion_scolarite;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDate;

public class ClientEnseignant extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    private String idEnseignant;
    private String nomComplet;
    private String anneeActuelle = "2025-2026";
    
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    public ClientEnseignant() {
        setTitle("Gestion Scolarité - Espace Enseignant");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        afficherPageConnexion();
    }
    
    private void afficherPageConnexion() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(255, 248, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("Connexion Enseignant");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(204, 102, 0));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        JLabel iconLabel = new JLabel("👨‍🏫");
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 60));
        loginPanel.add(iconLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(new JLabel("Login: "), gbc);
        
        JTextField loginField = new JTextField(20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(loginField, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(new JLabel("Mot de passe: "), gbc);
        
        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(passwordField, gbc);
        
        JButton loginButton = new JButton("Se connecter");
        loginButton.setBackground(new Color(255, 140, 0));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);
        
        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        gbc.gridy = 5;
        loginPanel.add(errorLabel, gbc);
        
        JLabel infoLabel = new JLabel("<html><i>Test: prof.dupont / prof123</i></html>");
        infoLabel.setForeground(Color.GRAY);
        gbc.gridy = 6;
        loginPanel.add(infoLabel, gbc);
        
        loginButton.addActionListener(e -> {
            String login = loginField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (login.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Veuillez remplir tous les champs");
                return;
            }
            
            if (connecterAuServeur(login, password)) {
                getContentPane().removeAll();
                afficherInterfacePrincipale();
                revalidate();
                repaint();
            } else {
                errorLabel.setText("Identifiants incorrects");
            }
        });
        
        passwordField.addActionListener(e -> loginButton.doClick());
        setContentPane(loginPanel);
    }
    
    private boolean connecterAuServeur(String login, String password) {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            out.println("AUTH|" + login + "|" + password);
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                String typeUser = parts[1];
                
                if (!typeUser.equals("ENSEIGNANT")) {
                    JOptionPane.showMessageDialog(this, 
                        "Cette interface est réservée aux enseignants", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    socket.close();
                    return false;
                }
                
                idEnseignant = parts[2];
                nomComplet = parts[3];
                return true;
            }
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur de connexion au serveur: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void afficherInterfacePrincipale() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 140, 0));
        topPanel.setPreferredSize(new Dimension(0, 80));
        
        JLabel welcomeLabel = new JLabel("  👨‍🏫 Prof. " + nomComplet);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> deconnecter());
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(60, 60, 60));
        menuPanel.setPreferredSize(new Dimension(220, 0));
        
        String[] menuItems = {
            "🏠 Accueil",
            "➕ Créer Épreuve",
            "✏️ Saisir Notes",
            "📝 Mes Épreuves",
            "📊 Résultats",
            "🧮 Calculer Notes"
        };
        
        for (String item : menuItems) {
            JButton menuBtn = createMenuButton(item);
            menuPanel.add(menuBtn);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Color.WHITE);
        
        mainPanel.add(createAccueilPanel(), "accueil");
        mainPanel.add(createCreerEpreuvePanel(), "creer");
        mainPanel.add(createSaisirNotesPanel(), "saisir");
        mainPanel.add(createMesEpreuvesPanel(), "epreuves");
        mainPanel.add(createResultatsPanel(), "resultats");
        mainPanel.add(createCalculerPanel(), "calculer");
        
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
        
        cardLayout.show(mainPanel, "accueil");
    }
    
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 50));
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(255, 140, 0));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(60, 60, 60));
            }
        });
        
        btn.addActionListener(e -> {
            if (text.contains("Accueil")) cardLayout.show(mainPanel, "accueil");
            else if (text.contains("Créer")) cardLayout.show(mainPanel, "creer");
            else if (text.contains("Saisir")) cardLayout.show(mainPanel, "saisir");
            else if (text.contains("Épreuves")) cardLayout.show(mainPanel, "epreuves");
            else if (text.contains("Résultats")) cardLayout.show(mainPanel, "resultats");
            else if (text.contains("Calculer")) cardLayout.show(mainPanel, "calculer");
        });
        
        return btn;
    }
    
    private JPanel createAccueilPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Bienvenue dans l'espace Enseignant", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        cardsPanel.setBackground(Color.WHITE);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        
        cardsPanel.add(createDashCard("➕", "Créer Épreuve", new Color(0, 123, 255)));
        cardsPanel.add(createDashCard("✏️", "Saisir Notes", new Color(40, 167, 69)));
        cardsPanel.add(createDashCard("📝", "Mes Épreuves", new Color(108, 117, 125)));
        cardsPanel.add(createDashCard("📊", "Résultats", new Color(255, 193, 7)));
        cardsPanel.add(createDashCard("🧮", "Calculer", new Color(220, 53, 69)));
        cardsPanel.add(createDashCard("👥", "Étudiants", new Color(23, 162, 184)));
        
        panel.add(cardsPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createDashCard(String icon, String title, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(color.darker(), 2));
        
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setForeground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);
        
        card.add(Box.createVerticalGlue());
        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(titleLabel);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
    
    private JPanel createCreerEpreuvePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Créer une Nouvelle Épreuve", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Matière
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Matière:"), gbc);
        
        JComboBox<String> matiereCombo = new JComboBox<>();
        chargerMatieres(matiereCombo);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(matiereCombo, gbc);
        
        // Type d'épreuve
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Type:"), gbc);
        
        String[] types = {"CONTROLE", "EXAMEN", "PROJET", "TP", "TD", "AUTRE"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(typeCombo, gbc);
        
        // Libellé
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Libellé:"), gbc);
        
        JTextField libelleField = new JTextField(30);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(libelleField, gbc);
        
        // Coefficient
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Coefficient:"), gbc);
        
        JSpinner coeffSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.1));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(coeffSpinner, gbc);
        
        // Date
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date:"), gbc);
        
        JTextField dateField = new JTextField(LocalDate.now().toString(), 15);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(dateField, gbc);
        
        // Note sur
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Note sur:"), gbc);
        
        JSpinner noteSurSpinner = new JSpinner(new SpinnerNumberModel(20.0, 1.0, 100.0, 1.0));
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(noteSurSpinner, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Description:"), gbc);
        
        JTextArea descArea = new JTextArea(4, 30);
        descArea.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(descScroll, gbc);
        
        // Bouton créer
        JButton creerButton = new JButton("Créer l'Épreuve");
        creerButton.setBackground(new Color(40, 167, 69));
        creerButton.setForeground(Color.WHITE);
        creerButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(creerButton, gbc);
        
        creerButton.addActionListener(e -> {
            String matiereStr = (String) matiereCombo.getSelectedItem();
            if (matiereStr == null || libelleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs obligatoires");
                return;
            }
            
            String idMatiere = matiereStr.split(" - ")[0];
            String type = (String) typeCombo.getSelectedItem();
            String libelle = libelleField.getText().trim();
            double coeff = (Double) coeffSpinner.getValue();
            String date = dateField.getText();
            double noteSur = (Double) noteSurSpinner.getValue();
            String desc = descArea.getText().trim();
            
            try {
                out.println("CREATE_EPREUVE|" + idMatiere + "|" + idEnseignant + "|" + 
                           anneeActuelle + "|" + type + "|" + libelle + "|" + coeff + "|" + 
                           date + "|" + noteSur + "|" + desc);
                String response = in.readLine();
                
                if (response.startsWith("SUCCESS")) {
                    JOptionPane.showMessageDialog(this, "Épreuve créée avec succès!", 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    libelleField.setText("");
                    descArea.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur: " + response.split("\\|")[1], 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createSaisirNotesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Saisir les Notes", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        
        topPanel.add(new JLabel("Sélectionner une épreuve: "));
        JComboBox<String> epreuveCombo = new JComboBox<>();
        chargerEpreuves(epreuveCombo);
        topPanel.add(epreuveCombo);
        
        JButton chargerButton = new JButton("Charger les Étudiants");
        chargerButton.setBackground(new Color(0, 123, 255));
        chargerButton.setForeground(Color.WHITE);
        topPanel.add(chargerButton);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Nom", "Prénom", "Note", "Absent"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("💾 Enregistrer les Notes");
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        bottomPanel.add(saveButton);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        chargerButton.addActionListener(e -> {
            model.setRowCount(0);
            try {
                out.println("GET_ETUDIANTS");
                String response = in.readLine();
                
                if (response.startsWith("SUCCESS")) {
                    String[] parts = response.split("\\|");
                    for (int i = 1; i < parts.length; i++) {
                        String[] etudData = parts[i].split(";");
                        model.addRow(new Object[]{
                            etudData[0], // ID
                            etudData[1], // Nom
                            etudData[2], // Prénom
                            "", // Note vide
                            false // Absent
                        });
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        
        saveButton.addActionListener(e -> {
            String epreuveStr = (String) epreuveCombo.getSelectedItem();
            if (epreuveStr == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une épreuve");
                return;
            }
            
            String idEpreuve = epreuveStr.split(" - ")[0];
            StringBuilder notesData = new StringBuilder("SAISIR_NOTES|" + idEpreuve);
            
            for (int i = 0; i < model.getRowCount(); i++) {
                String idEtud = model.getValueAt(i, 0).toString();
                String note = model.getValueAt(i, 3).toString();
                boolean absent = (Boolean) model.getValueAt(i, 4);
                
                notesData.append("|").append(idEtud).append(":");
                notesData.append(note.isEmpty() ? "0" : note).append(":");
                notesData.append(absent);
            }
            
            try {
                out.println(notesData.toString());
                String response = in.readLine();
                
                if (response.startsWith("SUCCESS")) {
                    JOptionPane.showMessageDialog(this, "Notes enregistrées avec succès!", 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur: " + response);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        
        return panel;
    }
    
    private JPanel createMesEpreuvesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Mes Épreuves", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Matière", "Libellé", "Type", "Coefficient", "Date", "Année", "Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        try {
            out.println("GET_EPREUVES");
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                for (int i = 1; i < parts.length; i++) {
                    String[] eprData = parts[i].split(";");
                    if (eprData.length >= 8) {
                        model.addRow(new Object[]{
                            eprData[0], eprData[1], eprData[2], eprData[3],
                            eprData[4], eprData[5], eprData[6], eprData[7]
                        });
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(255, 140, 0));
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton refreshButton = new JButton("🔄 Actualiser");
        refreshButton.setBackground(new Color(0, 123, 255));
        refreshButton.setForeground(Color.WHITE);
        buttonPanel.add(refreshButton);
        
        JButton deleteButton = new JButton("🗑️ Supprimer");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        buttonPanel.add(deleteButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        refreshButton.addActionListener(e -> {
            model.setRowCount(0);
            try {
                out.println("GET_EPREUVES");
                String response = in.readLine();
                if (response.startsWith("SUCCESS")) {
                    String[] parts = response.split("\\|");
                    for (int i = 1; i < parts.length; i++) {
                        String[] eprData = parts[i].split(";");
                        if (eprData.length >= 8) {
                            model.addRow(new Object[]{
                                eprData[0], eprData[1], eprData[2], eprData[3],
                                eprData[4], eprData[5], eprData[6], eprData[7]
                            });
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String idEpreuve = model.getValueAt(row, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Êtes-vous sûr de vouloir supprimer cette épreuve?", 
                    "Confirmation", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        out.println("SUPPRIMER_EPREUVE|" + idEpreuve);
                        String response = in.readLine();
                        if (response.startsWith("SUCCESS")) {
                            model.removeRow(row);
                            JOptionPane.showMessageDialog(this, "Épreuve supprimée");
                        } else {
                            JOptionPane.showMessageDialog(this, "Erreur: " + response);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une épreuve");
            }
        });
        
        return panel;
    }
    
    private JPanel createResultatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Résultats des Étudiants", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Matière: "));
        
        JComboBox<String> matiereCombo = new JComboBox<>();
        chargerMatieres(matiereCombo);
        topPanel.add(matiereCombo);
        
        JButton chargerButton = new JButton("Charger les Résultats");
        chargerButton.setBackground(new Color(0, 123, 255));
        chargerButton.setForeground(Color.WHITE);
        topPanel.add(chargerButton);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Nom", "Prénom", "Note Finale", "Nb Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        chargerButton.addActionListener(e -> {
            String matiereStr = (String) matiereCombo.getSelectedItem();
            if (matiereStr == null) return;
            
            String idMatiere = matiereStr.split(" - ")[0];
            model.setRowCount(0);
            
            try {
                out.println("GET_RESULTATS_MATIERE|" + idMatiere + "|" + anneeActuelle);
                String response = in.readLine();
                
                if (response.startsWith("SUCCESS")) {
                    String[] parts = response.split("\\|");
                    for (int i = 1; i < parts.length; i++) {
                        String[] resData = parts[i].split(";");
                        model.addRow(new Object[]{
                            resData[0], resData[1], resData[2], resData[3], resData[4]
                        });
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        
        return panel;
    }
    
    private JPanel createCalculerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Calculer les Notes Finales", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Étudiant:"), gbc);
        
        JComboBox<String> etudiantCombo = new JComboBox<>();
        chargerEtudiants(etudiantCombo);
        gbc.gridx = 1;
        centerPanel.add(etudiantCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(new JLabel("Matière:"), gbc);
        
        JComboBox<String> matiereCombo = new JComboBox<>();
        chargerMatieres(matiereCombo);
        gbc.gridx = 1;
        centerPanel.add(matiereCombo, gbc);
        
        JButton calculerButton = new JButton("🧮 Calculer la Note Finale");
        calculerButton.setBackground(new Color(40, 167, 69));
        calculerButton.setForeground(Color.WHITE);
        calculerButton.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        centerPanel.add(calculerButton, gbc);
        
        JLabel resultLabel = new JLabel("");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridy = 3;
        centerPanel.add(resultLabel, gbc);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        calculerButton.addActionListener(e -> {
            String etudStr = (String) etudiantCombo.getSelectedItem();
            String matStr = (String) matiereCombo.getSelectedItem();
            
            if (etudStr == null || matStr == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un étudiant et une matière");
                return;
            }
            
            String idEtud = etudStr.split(" - ")[0];
            String idMat = matStr.split(" - ")[0];
            
            try {
                out.println("CALCULER_NOTE_MATIERE|" + idEtud + "|" + idMat + "|" + anneeActuelle);
                String response = in.readLine();
                
                if (response.startsWith("SUCCESS")) {
                    String note = response.split("\\|")[1];
                    resultLabel.setText("Note calculée: " + note + " / 20");
                    resultLabel.setForeground(new Color(40, 167, 69));
                } else {
                    resultLabel.setText("Erreur: " + response.split("\\|")[1]);
                    resultLabel.setForeground(Color.RED);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        
        return panel;
    }
    
    private void chargerMatieres(JComboBox<String> combo) {
        try {
            out.println("GET_MATIERES");
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                for (int i = 1; i < parts.length; i++) {
                    String[] matData = parts[i].split(";");
                    combo.addItem(matData[0] + " - " + matData[2]); // ID - Nom
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void chargerEpreuves(JComboBox<String> combo) {
        try {
            out.println("GET_EPREUVES");
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                for (int i = 1; i < parts.length; i++) {
                    String[] eprData = parts[i].split(";");
                    combo.addItem(eprData[0] + " - " + eprData[2] + " (" + eprData[1] + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void chargerEtudiants(JComboBox<String> combo) {
        try {
            out.println("GET_ETUDIANTS");
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                for (int i = 1; i < parts.length; i++) {
                    String[] etudData = parts[i].split(";");
                    combo.addItem(etudData[0] + " - " + etudData[1] + " " + etudData[2]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void deconnecter() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dispose();
        new ClientEnseignant().setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ClientEnseignant().setVisible(true);
        });
    }
}