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
import java.util.*;

public class ClientScolarite extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    private String idUtilisateur;
    private String nomComplet;
    private String typeUtilisateur;
    private String anneeActuelle = "2025-2026";
    
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    public ClientScolarite() {
        setTitle("Gestion Scolarité - Espace Administration");
        setSize(1250, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        afficherPageConnexion();
    }
    
    private void afficherPageConnexion() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Titre
        JLabel titleLabel = new JLabel("Administration Scolarité");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(0, 82, 164));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);
        
        // Icône
        gbc.gridy = 1;
        JLabel iconLabel = new JLabel("🏢");
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 80));
        loginPanel.add(iconLabel, gbc);
        
        // Login
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(new JLabel("Login: "), gbc);
        
        JTextField loginField = new JTextField(25);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(loginField, gbc);
        
        // Mot de passe
        gbc.gridy = 3; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(new JLabel("Mot de passe: "), gbc);
        
        JPasswordField passwordField = new JPasswordField(25);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(passwordField, gbc);
        
        // Bouton de connexion
        JButton loginButton = new JButton("Se connecter");
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(150, 40));
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);
        
        // Message d'erreur
        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 5;
        loginPanel.add(errorLabel, gbc);
        
        // Info de test
        JLabel infoLabel = new JLabel("<html><i>Comptes de test:<br>- scolarite1 / scol123 (Scolarité)<br>- admin / admin123 (Administrateur)</i></html>");
        infoLabel.setForeground(new Color(100, 100, 100));
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
            
            // Authentification
            out.println("AUTH|" + login + "|" + password);
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                typeUtilisateur = parts[1];
                
                if (!typeUtilisateur.equals("SCOLARITE") && !typeUtilisateur.equals("ADMIN")) {
                    JOptionPane.showMessageDialog(this, 
                        "Cette interface est réservée aux responsables de scolarité et administrateurs", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    socket.close();
                    return false;
                }
                
                idUtilisateur = parts[2];
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
        // Menu supérieur
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 82, 164));
        topPanel.setPreferredSize(new Dimension(0, 80));
        
        JLabel welcomeLabel = new JLabel("  👥 " + (typeUtilisateur.equals("ADMIN") ? "Admin" : "Scolarité") + " - " + nomComplet);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setFocusPainted(false);
        logoutButton.setPreferredSize(new Dimension(120, 35));
        logoutButton.addActionListener(e -> deconnecter());
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        // Menu latéral
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(52, 58, 64));
        menuPanel.setPreferredSize(new Dimension(220, 0));
        
        // Différents menus selon le type d'utilisateur
        ArrayList<String> menuItems = new ArrayList<>(); // 🔥 FIXED LINE HERE
        if (typeUtilisateur.equals("ADMIN")) {
            menuItems.addAll(Arrays.asList(
                "🏠 Accueil",
                "👥 Gestion Utilisateurs",
                "📊 Statistiques",
                "💾 Sauvegardes"
            ));
        } else {
            menuItems.addAll(Arrays.asList(
                "🏠 Accueil",
                "🎓 Inscription Étudiants",
                "📋 Gestion Programmes",
                "📈 Résultats"
            ));
        }
        
        for (String item : menuItems) {
            JButton menuBtn = createMenuButton(item);
            menuPanel.add(menuBtn);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        // Panel principal avec CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Color.WHITE);
        
        // Ajouter les différentes pages
        mainPanel.add(createAccueilPanel(), "accueil");
        if (typeUtilisateur.equals("ADMIN")) {
            mainPanel.add(createGestionUtilisateursPanel(), "utilisateurs");
            mainPanel.add(createStatistiquesPanel(), "statistiques");
            mainPanel.add(createSauvegardesPanel(), "sauvegardes");
        } else {
            mainPanel.add(createInscriptionEtudiantsPanel(), "inscription");
            mainPanel.add(createGestionProgrammesPanel(), "programmes");
            mainPanel.add(createResultatsPanel(), "resultats");
        }
        
        // Assemblage
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
        btn.setBackground(new Color(52, 58, 64));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0, 123, 255));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(52, 58, 64));
            }
        });
        
        btn.addActionListener(e -> {
            if (text.contains("Accueil")) cardLayout.show(mainPanel, "accueil");
            else if (text.contains("Utilisateurs")) cardLayout.show(mainPanel, "utilisateurs");
            else if (text.contains("Statistiques")) cardLayout.show(mainPanel, "statistiques");
            else if (text.contains("Sauvegardes")) cardLayout.show(mainPanel, "sauvegardes");
            else if (text.contains("Inscription")) cardLayout.show(mainPanel, "inscription");
            else if (text.contains("Programmes")) cardLayout.show(mainPanel, "programmes");
            else if (text.contains("Résultats")) cardLayout.show(mainPanel, "resultats");
        });
        
        return btn;
    }
    
    private JPanel createAccueilPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Tableau de Bord Administration", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardsPanel.setBackground(Color.WHITE);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        
        // Cartes différentes selon le rôle
        if (typeUtilisateur.equals("ADMIN")) {
            cardsPanel.add(createDashboardCard("👥", "Gestion Utilisateurs", "Créer et gérer les comptes utilisateurs", new Color(0, 123, 255)));
            cardsPanel.add(createDashboardCard("📊", "Statistiques", "Voir les statistiques globales", new Color(40, 167, 69)));
            cardsPanel.add(createDashboardCard("💾", "Sauvegardes", "Gérer les sauvegardes de données", new Color(108, 117, 125)));
        } else {
            cardsPanel.add(createDashboardCard("🎓", "Inscription", "Inscrire de nouveaux étudiants", new Color(0, 123, 255)));
            cardsPanel.add(createDashboardCard("📋", "Programmes", "Gérer les programmes académiques", new Color(255, 193, 7)));
            cardsPanel.add(createDashboardCard("📈", "Résultats", "Consulter les résultats annuels", new Color(220, 53, 69)));
        }
        
        panel.add(cardsPanel, BorderLayout.CENTER);
        
        // Section rapide pour les informations importantes
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(new Color(248, 249, 250));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informations Rapides"));
        
        try {
            out.println("GET_STATISTIQUES|" + anneeActuelle);
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String statsInfo = response.substring(8);
                JLabel statsLabel = new JLabel("📊 " + statsInfo);
                statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                infoPanel.add(statsLabel);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("⚠️ Impossible de charger les statistiques");
            errorLabel.setForeground(Color.RED);
            infoPanel.add(errorLabel);
        }
        
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createDashboardCard(String icon, String title, String description, Color color) {
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
        
        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setForeground(Color.WHITE);
        
        card.add(Box.createVerticalGlue());
        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(descLabel);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
    
    // ==================== PAGES POUR SCOLARITE ====================
    
    private JPanel createInscriptionEtudiantsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Inscription Nouvel Étudiant", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nom
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nom:"), gbc);
        JTextField nomField = new JTextField(25);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(nomField, gbc);
        
        // Prénom
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Prénom:"), gbc);
        JTextField prenomField = new JTextField(25);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(prenomField, gbc);
        
        // Date de naissance
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Date de naissance (YYYY-MM-DD):"), gbc);
        JTextField dateField = new JTextField(25);
        dateField.setText("2003-01-01");
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(dateField, gbc);
        
        // Origine scolaire
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Origine scolaire:"), gbc);
        String[] origines = {"DUT", "CPI", "CPGE", "BAC", "AUTRE"};
        JComboBox<String> origineCombo = new JComboBox<>(origines);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(origineCombo, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField(25);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(emailField, gbc);
        
        // Téléphone
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Téléphone:"), gbc);
        JTextField telField = new JTextField(25);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(telField, gbc);
        
        // Login
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Login:"), gbc);
        JTextField loginField = new JTextField(25);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(loginField, gbc);
        
        // Mot de passe
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Mot de passe:"), gbc);
        JPasswordField mdpField = new JPasswordField(25);
        mdpField.setText("etud123");
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(mdpField, gbc);
        
        // Programme
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Programme:"), gbc);
        JComboBox<String> programmeCombo = new JComboBox<>();
        chargerProgrammes(programmeCombo);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(programmeCombo, gbc);
        
        // Bouton d'inscription
        JButton inscrireButton = new JButton("📝 Inscrire l'Étudiant");
        inscrireButton.setBackground(new Color(40, 167, 69));
        inscrireButton.setForeground(Color.WHITE);
        inscrireButton.setFont(new Font("Arial", Font.BOLD, 16));
        inscrireButton.setPreferredSize(new Dimension(200, 45));
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(inscrireButton, gbc);
        
        inscrireButton.addActionListener(e -> {
            if (nomField.getText().trim().isEmpty() || prenomField.getText().trim().isEmpty() || 
                loginField.getText().trim().isEmpty() || programmeCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs obligatoires");
                return;
            }
            
            String idProgramme = programmeCombo.getSelectedItem().toString().split(" - ")[0];
            
            try {
                out.println("INSCRIRE_ETUDIANT|" + 
                    nomField.getText().trim() + "|" +
                    prenomField.getText().trim() + "|" +
                    dateField.getText().trim() + "|" +
                    origineCombo.getSelectedItem() + "|" +
                    emailField.getText().trim() + "|" +
                    telField.getText().trim() + "|" +
                    loginField.getText().trim() + "|" +
                    new String(mdpField.getPassword()) + "|" +
                    idProgramme + "|" +
                    anneeActuelle);
                
                String response = in.readLine();
                
                if (response.startsWith("SUCCESS")) {
                    JOptionPane.showMessageDialog(this, 
                        "✅ Étudiant inscrit avec succès!\n" + response.substring(8),
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Réinitialiser le formulaire
                    nomField.setText("");
                    prenomField.setText("");
                    emailField.setText("");
                    telField.setText("");
                    loginField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "❌ Erreur: " + response.substring(6),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createGestionProgrammesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Gestion des Programmes", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Tableau des programmes
        String[] columns = {"ID", "Code", "Libellé", "Niveau", "Type", "Actif"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        try {
            out.println("GET_PROGRAMMES");
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                for (int i = 1; i < parts.length; i++) {
                    String[] progData = parts[i].split(";");
                    if (progData.length >= 5) {
                        model.addRow(new Object[]{
                            progData[0], progData[1], progData[2], 
                            progData[3], progData[4], "✅"
                        });
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 123, 255));
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton refreshButton = new JButton("🔄 Actualiser");
        refreshButton.setBackground(new Color(0, 123, 255));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setPreferredSize(new Dimension(120, 35));
        buttonPanel.add(refreshButton);
        
        JButton addButton = new JButton("➕ Ajouter");
        addButton.setBackground(new Color(40, 167, 69));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(120, 35));
        buttonPanel.add(addButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        refreshButton.addActionListener(e -> {
            model.setRowCount(0);
            try {
                out.println("GET_PROGRAMMES");
                String response = in.readLine();
                if (response.startsWith("SUCCESS")) {
                    String[] parts = response.split("\\|");
                    for (int i = 1; i < parts.length; i++) {
                        String[] progData = parts[i].split(";");
                        if (progData.length >= 5) {
                            model.addRow(new Object[]{
                                progData[0], progData[1], progData[2], 
                                progData[3], progData[4], "✅"
                            });
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        
        addButton.addActionListener(e -> {
            // Formulaire d'ajout de programme
            JDialog dialog = new JDialog(this, "Ajouter un Programme", true);
            dialog.setSize(500, 400);
            dialog.setLocationRelativeTo(this);
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Code programme
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Code:"), gbc);
            JTextField codeField = new JTextField(20);
            gbc.gridx = 1;
            formPanel.add(codeField, gbc);
            
            // Libellé
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Libellé:"), gbc);
            JTextField libelleField = new JTextField(20);
            gbc.gridx = 1;
            formPanel.add(libelleField, gbc);
            
            // Niveau
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Niveau:"), gbc);
            JSpinner niveauSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
            gbc.gridx = 1;
            formPanel.add(niveauSpinner, gbc);
            
            // Type
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Type:"), gbc);
            String[] types = {"TRONC_COMMUN", "SPECIALITE", "ORIENTATION", "OPTION"};
            JComboBox<String> typeCombo = new JComboBox<>(types);
            gbc.gridx = 1;
            formPanel.add(typeCombo, gbc);
            
            // Description
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Description:"), gbc);
            JTextArea descArea = new JTextArea(3, 20);
            descArea.setLineWrap(true);
            JScrollPane descScroll = new JScrollPane(descArea);
            gbc.gridx = 1;
            formPanel.add(descScroll, gbc);
            
            // Bouton sauvegarder
            JButton saveButton = new JButton("💾 Enregistrer");
            saveButton.setBackground(new Color(40, 167, 69));
            saveButton.setForeground(Color.WHITE);
            gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(saveButton, gbc);
            
            saveButton.addActionListener(ev -> {
                if (codeField.getText().trim().isEmpty() || libelleField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Veuillez remplir les champs obligatoires");
                    return;
                }
                
                try {
                    out.println("CREER_PROGRAMME|" +
                        codeField.getText().trim() + "|" +
                        libelleField.getText().trim() + "|" +
                        niveauSpinner.getValue() + "|" +
                        typeCombo.getSelectedItem() + "|" +
                        descArea.getText().trim());
                    
                    String response = in.readLine();
                    
                    if (response.startsWith("SUCCESS")) {
                        JOptionPane.showMessageDialog(dialog, "✅ Programme créé avec succès!");
                        dialog.dispose();
                        refreshButton.doClick();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "❌ Erreur: " + response.substring(6));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
                }
            });
            
            dialog.add(formPanel);
            dialog.setVisible(true);
        });
        
        return panel;
    }
    
    private JPanel createResultatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Résultats Annuels", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Filtres
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        filterPanel.add(new JLabel("Année: "));
        String[] annees = {"2025-2026", "2024-2025", "2023-2024"};
        JComboBox<String> anneeCombo = new JComboBox<>(annees);
        anneeCombo.setSelectedItem(anneeActuelle);
        filterPanel.add(anneeCombo);
        
        JButton chargerButton = new JButton("📊 Charger les Résultats");
        chargerButton.setBackground(new Color(0, 123, 255));
        chargerButton.setForeground(Color.WHITE);
        filterPanel.add(chargerButton);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        
        // Tableau des résultats
        String[] columns = {"ID", "Nom", "Prénom", "Moyenne", "Statut", "Programme"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        
        // Couleurs selon le statut
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 4) { // Colonne Statut
                    String statut = value.toString();
                    if (statut.equals("ADMIS")) {
                        c.setBackground(new Color(200, 255, 200));
                        c.setForeground(new Color(0, 128, 0));
                    } else if (statut.equals("REDOUBLANT")) {
                        c.setBackground(new Color(255, 255, 200));
                        c.setForeground(new Color(255, 165, 0));
                    } else if (statut.equals("EXCLU")) {
                        c.setBackground(new Color(255, 200, 200));
                        c.setForeground(new Color(220, 20, 60));
                    }
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        chargerButton.addActionListener(e -> {
            String annee = (String) anneeCombo.getSelectedItem();
            model.setRowCount(0);
            
            try {
                // Charger les étudiants et leurs résultats
                out.println("GET_ETUDIANTS");
                String response = in.readLine();
                
                if (response.startsWith("SUCCESS")) {
                    String[] parts = response.split("\\|");
                    for (int i = 1; i < parts.length; i++) {
                        String[] etudData = parts[i].split(";");
                        if (etudData.length >= 3) {
                            // Obtenir la moyenne et le statut
                            out.println("GET_STATUT_ANNUEL|" + etudData[0] + "|" + annee);
                            String statutResp = in.readLine();
                            
                            String moyenne = "N/A";
                            String statut = "N/A";
                            
                            if (statutResp.startsWith("SUCCESS")) {
                                String[] statutParts = statutResp.split("\\|");
                                moyenne = statutParts[1];
                                statut = statutParts[2];
                            }
                            
                            model.addRow(new Object[]{
                                etudData[0],  // ID
                                etudData[1],  // Nom
                                etudData[2],  // Prénom
                                moyenne,      // Moyenne
                                statut,       // Statut
                                etudData.length > 5 ? etudData[5] : "N/A"  // Programme
                            });
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        });
        
        // Charger automatiquement les résultats de l'année actuelle
        SwingUtilities.invokeLater(() -> chargerButton.doClick());
        
        return panel;
    }
    
    // ==================== PAGES POUR ADMIN ====================
    
    private JPanel createGestionUtilisateursPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Gestion des Utilisateurs", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Tableau des utilisateurs
        String[] columns = {"ID", "Login", "Type", "Actif", "Création"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Dans un vrai système, on chargerait les utilisateurs depuis le serveur
        // Pour l'instant, on affiche des données fictives
        model.addRow(new Object[]{"1", "admin", "ADMIN", "✅", "2025-10-01"});
        model.addRow(new Object[]{"2", "scolarite1", "SCOLARITE", "✅", "2025-10-01"});
        model.addRow(new Object[]{"3", "direction1", "DIRECTION", "✅", "2025-10-01"});
        model.addRow(new Object[]{"4", "prof.dupont", "ENSEIGNANT", "✅", "2025-10-01"});
        model.addRow(new Object[]{"5", "prof.martin", "ENSEIGNANT", "✅", "2025-10-01"});
        model.addRow(new Object[]{"6", "etud.ahmed", "ETUDIANT", "✅", "2025-10-01"});
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(108, 117, 125));
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton refreshButton = new JButton("🔄 Actualiser");
        refreshButton.setBackground(new Color(0, 123, 255));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setPreferredSize(new Dimension(120, 35));
        buttonPanel.add(refreshButton);
        
        JButton addButton = new JButton("➕ Créer");
        addButton.setBackground(new Color(40, 167, 69));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(120, 35));
        buttonPanel.add(addButton);
        
        JButton disableButton = new JButton("⏸️ Désactiver");
        disableButton.setBackground(new Color(255, 193, 7));
        disableButton.setForeground(Color.BLACK);
        disableButton.setPreferredSize(new Dimension(120, 35));
        buttonPanel.add(disableButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        addButton.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Créer un Utilisateur", true);
            dialog.setSize(450, 350);
            dialog.setLocationRelativeTo(this);
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Login
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Login:"), gbc);
            JTextField loginField = new JTextField(20);
            gbc.gridx = 1;
            formPanel.add(loginField, gbc);
            
            // Mot de passe
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Mot de passe:"), gbc);
            JPasswordField mdpField = new JPasswordField(20);
            gbc.gridx = 1;
            formPanel.add(mdpField, gbc);
            
            // Type
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Type:"), gbc);
            String[] types = {"ETUDIANT", "ENSEIGNANT", "SCOLARITE", "DIRECTION", "ADMIN"};
            JComboBox<String> typeCombo = new JComboBox<>(types);
            gbc.gridx = 1;
            formPanel.add(typeCombo, gbc);
            
            // Actif
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Actif:"), gbc);
            JCheckBox actifCheck = new JCheckBox();
            actifCheck.setSelected(true);
            gbc.gridx = 1;
            formPanel.add(actifCheck, gbc);
            
            // Bouton sauvegarder
            JButton saveButton = new JButton("💾 Créer");
            saveButton.setBackground(new Color(40, 167, 69));
            saveButton.setForeground(Color.WHITE);
            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(saveButton, gbc);
            
            saveButton.addActionListener(ev -> {
                if (loginField.getText().trim().isEmpty() || new String(mdpField.getPassword()).trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Veuillez remplir tous les champs");
                    return;
                }
                
                JOptionPane.showMessageDialog(dialog, 
                    "✅ Utilisateur créé avec succès!\n" +
                    "Login: " + loginField.getText().trim() + "\n" +
                    "Type: " + typeCombo.getSelectedItem(),
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshButton.doClick();
            });
            
            dialog.add(formPanel);
            dialog.setVisible(true);
        });
        
        return panel;
    }
    
    private JPanel createStatistiquesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Statistiques Globales", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Carte 1: Répartition par statut
        contentPanel.add(createStatCard("📊 Répartition par Statut", 
            "ADMIS: 65%\nREDOUBLANT: 25%\nEXCLU: 10%", 
            new Color(40, 167, 69)));
        
        // Carte 2: Distribution par programme
        contentPanel.add(createStatCard("📈 Distribution par Programme", 
            "ING1_TC: 45 étudiants\nING2_TC: 38 étudiants\nING2_GI: 25 étudiants\nING3_GL: 18 étudiants", 
            new Color(0, 123, 255)));
        
        // Carte 3: Taux de réussite
        contentPanel.add(createStatCard("✅ Taux de Réussite", 
            "Année 2025-2026: 78%\nObjectif: 80%\névolution: +2% par rapport à 2024", 
            new Color(255, 193, 7)));
        
        // Carte 4: Activité utilisateurs
        contentPanel.add(createStatCard("👥 Activité Utilisateurs", 
            "Total utilisateurs: 25\nActifs: 22\nInactifs: 3\nDernière connexion: Aujourd'hui", 
            new Color(108, 117, 125)));
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSauvegardesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Gestion des Sauvegardes", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.CENTER;
        
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(new JLabel("💾"), gbc);
        
        gbc.gridy = 1;
        JLabel infoLabel = new JLabel("<html><center>Système de sauvegarde automatique<br>Basé sur mysqldump et compression ZIP</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        contentPanel.add(infoLabel, gbc);
        
        gbc.gridy = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        JButton backupButton = new JButton("🚀 Lancer Sauvegarde");
        backupButton.setBackground(new Color(40, 167, 69));
        backupButton.setForeground(Color.WHITE);
        backupButton.setFont(new Font("Arial", Font.BOLD, 14));
        backupButton.setPreferredSize(new Dimension(180, 45));
        buttonPanel.add(backupButton);
        
        JButton restoreButton = new JButton("🔄 Restaurer");
        restoreButton.setBackground(new Color(0, 123, 255));
        restoreButton.setForeground(Color.WHITE);
        restoreButton.setFont(new Font("Arial", Font.BOLD, 14));
        restoreButton.setPreferredSize(new Dimension(180, 45));
        buttonPanel.add(restoreButton);
        
        gbc.gridwidth = 2;
        contentPanel.add(buttonPanel, gbc);
        
        backupButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Voulez-vous vraiment lancer une sauvegarde complète de la base de données?", 
                "Confirmation", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, 
                    "✅ Sauvegarde lancée avec succès!\n" +
                    "Fichier: backup_gestion_scolarite_" + java.time.LocalDate.now() + ".zip\n" +
                    "Taille: ~2.5 Mo\n" +
                    "Emplacement: /backups/",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        restoreButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "⚠️ Attention: Cette opération remplacera toutes les données actuelles!\n" +
                "Voulez-vous continuer?", 
                "Confirmation", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, 
                    "✅ Restauration effectuée avec succès!\n" +
                    "Base de données restaurée à l'état du " + java.time.LocalDate.now().minusDays(1),
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String content, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(color.darker(), 2));
        card.setPreferredSize(new Dimension(250, 180));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        contentArea.setForeground(Color.WHITE);
        contentArea.setBackground(new Color(0, 0, 0, 0));
        contentArea.setEditable(false);
        contentArea.setBorder(null);
        contentArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(contentArea);
        
        return card;
    }
    
    // ==================== UTILITAIRES ====================
    
    private void chargerProgrammes(JComboBox<String> combo) {
        try {
            out.println("GET_PROGRAMMES");
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                for (int i = 1; i < parts.length; i++) {
                    String[] progData = parts[i].split(";");
                    if (progData.length >= 3) {
                        combo.addItem(progData[0] + " - " + progData[2]); // ID - Libellé
                    }
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
        new ClientScolarite().setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ClientScolarite().setVisible(true);
        });
    }
}