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

public class ClientEtudiant extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    private String idEtudiant;
    private String nomComplet;
    private String anneeActuelle = "2025-2026";
    
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    public ClientEtudiant() {
        setTitle("Gestion Scolarité - Espace Étudiant");
        setSize(1000, 700);
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
        JLabel titleLabel = new JLabel("Connexion Étudiant");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);
        
        // Logo ou icône
        gbc.gridy = 1;
        JLabel iconLabel = new JLabel("🎓");
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 60));
        loginPanel.add(iconLabel, gbc);
        
        // Login
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(new JLabel("Login: "), gbc);
        
        JTextField loginField = new JTextField(20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(loginField, gbc);
        
        // Mot de passe
        gbc.gridy = 3; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(new JLabel("Mot de passe: "), gbc);
        
        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(passwordField, gbc);
        
        // Bouton de connexion
        JButton loginButton = new JButton("Se connecter");
        loginButton.setBackground(new Color(0, 153, 76));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);
        
        // Message d'erreur
        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        gbc.gridy = 5;
        loginPanel.add(errorLabel, gbc);
        
        // Info de test
        JLabel infoLabel = new JLabel("<html><i>Test: etud.ahmed / etud123</i></html>");
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
            
            // Authentification
            out.println("AUTH|" + login + "|" + password);
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                String typeUser = parts[1];
                
                if (!typeUser.equals("ETUDIANT")) {
                    JOptionPane.showMessageDialog(this, 
                        "Cette interface est réservée aux étudiants", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    socket.close();
                    return false;
                }
                
                idEtudiant = parts[2];
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
        topPanel.setBackground(new Color(0, 102, 204));
        topPanel.setPreferredSize(new Dimension(0, 80));
        
        JLabel welcomeLabel = new JLabel("  Bienvenue " + nomComplet);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> deconnecter());
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        // Menu latéral
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(52, 58, 64));
        menuPanel.setPreferredSize(new Dimension(200, 0));
        
        String[] menuItems = {
            "📋 Mes Informations",
            "📝 Mes Notes",
            "📊 Moyenne Générale",
            "🎯 Statut Annuel"
        };
        
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
        mainPanel.add(createInfoPanel(), "info");
        mainPanel.add(createNotesPanel(), "notes");
        mainPanel.add(createMoyennePanel(), "moyenne");
        mainPanel.add(createStatutPanel(), "statut");
        
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
        btn.setMaximumSize(new Dimension(200, 50));
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
            if (text.contains("Informations")) cardLayout.show(mainPanel, "info");
            else if (text.contains("Notes")) cardLayout.show(mainPanel, "notes");
            else if (text.contains("Moyenne")) cardLayout.show(mainPanel, "moyenne");
            else if (text.contains("Statut")) cardLayout.show(mainPanel, "statut");
        });
        
        return btn;
    }
    
    private JPanel createAccueilPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Tableau de Bord", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setBackground(Color.WHITE);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        
        cardsPanel.add(createDashboardCard("📋", "Informations", "Consultez vos informations personnelles", new Color(0, 123, 255)));
        cardsPanel.add(createDashboardCard("📝", "Notes", "Consultez vos notes par épreuve et matière", new Color(40, 167, 69)));
        cardsPanel.add(createDashboardCard("📊", "Moyenne", "Consultez votre moyenne générale", new Color(255, 193, 7)));
        cardsPanel.add(createDashboardCard("🎯", "Statut", "Consultez votre statut de fin d'année", new Color(220, 53, 69)));
        
        panel.add(cardsPanel, BorderLayout.CENTER);
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
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);
        
        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setForeground(Color.WHITE);
        
        card.add(Box.createVerticalGlue());
        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(descLabel);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Mes Informations Personnelles", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        
        try {
            out.println("GET_INFO_ETUDIANT|" + idEtudiant);
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(10, 10, 10, 10);
                gbc.anchor = GridBagConstraints.WEST;
                
                String[][] info = {
                    {"Nom:", parts[1]},
                    {"Prénom:", parts[2]},
                    {"Date de naissance:", parts[3]},
                    {"Origine scolaire:", parts[4]},
                    {"Email:", parts[5]},
                    {"Téléphone:", parts[6]},
                    {"Login:", parts[7]}
                };
                
                for (int i = 0; i < info.length; i++) {
                    gbc.gridx = 0; gbc.gridy = i;
                    JLabel label = new JLabel(info[i][0]);
                    label.setFont(new Font("Arial", Font.BOLD, 16));
                    infoPanel.add(label, gbc);
                    
                    gbc.gridx = 1;
                    JLabel value = new JLabel(info[i][1]);
                    value.setFont(new Font("Arial", Font.PLAIN, 16));
                    infoPanel.add(value, gbc);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
        
        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createNotesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Mes Notes", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Tableau des notes
        String[] columns = {"Matière", "Épreuve", "Type", "Note", "Coefficient", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        try {
            out.println("GET_NOTES_ETUDIANT|" + idEtudiant + "|" + anneeActuelle);
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                for (int i = 1; i < parts.length; i++) {
                    String[] noteData = parts[i].split(";");
                    if (noteData.length >= 6) {
                        model.addRow(new Object[]{
                            noteData[0], // Matière
                            noteData[1], // Épreuve
                            noteData[2], // Type
                            noteData[3], // Note
                            noteData[4], // Coefficient
                            noteData[5]  // Date
                        });
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
        
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 123, 255));
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMoyennePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Ma Moyenne Générale", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        
        try {
            out.println("GET_MOYENNE_GENERALE|" + idEtudiant + "|" + anneeActuelle);
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String moyenne = response.split("\\|")[1];
                double moyenneVal = Double.parseDouble(moyenne);
                
                JLabel moyenneLabel = new JLabel(moyenne + " / 20");
                moyenneLabel.setFont(new Font("Arial", Font.BOLD, 72));
                moyenneLabel.setForeground(moyenneVal >= 10 ? new Color(40, 167, 69) : new Color(220, 53, 69));
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0; gbc.gridy = 0;
                centerPanel.add(moyenneLabel, gbc);
                
                JLabel anneeLabel = new JLabel("Année " + anneeActuelle);
                anneeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                gbc.gridy = 1;
                centerPanel.add(anneeLabel, gbc);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Moyenne non disponible");
            errorLabel.setFont(new Font("Arial", Font.BOLD, 24));
            centerPanel.add(errorLabel);
        }
        
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStatutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Mon Statut Annuel", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        
        try {
            out.println("GET_STATUT_ANNUEL|" + idEtudiant + "|" + anneeActuelle);
            String response = in.readLine();
            
            if (response.startsWith("SUCCESS")) {
                String[] parts = response.split("\\|");
                String moyenne = parts[1];
                String statut = parts[2];
                
                String emoji = "";
                Color color = Color.BLACK;
                switch (statut) {
                    case "ADMIS":
                        emoji = "🎉";
                        color = new Color(40, 167, 69);
                        break;
                    case "REDOUBLANT":
                        emoji = "⚠️";
                        color = new Color(255, 193, 7);
                        break;
                    case "EXCLU":
                        emoji = "❌";
                        color = new Color(220, 53, 69);
                        break;
                }
                
                gbc.gridx = 0; gbc.gridy = 0;
                JLabel emojiLabel = new JLabel(emoji);
                emojiLabel.setFont(new Font("Arial", Font.PLAIN, 80));
                centerPanel.add(emojiLabel, gbc);
                
                gbc.gridy = 1;
                JLabel statutLabel = new JLabel(statut);
                statutLabel.setFont(new Font("Arial", Font.BOLD, 48));
                statutLabel.setForeground(color);
                centerPanel.add(statutLabel, gbc);
                
                gbc.gridy = 2;
                JLabel moyenneLabel = new JLabel("Moyenne: " + moyenne + " / 20");
                moyenneLabel.setFont(new Font("Arial", Font.PLAIN, 24));
                centerPanel.add(moyenneLabel, gbc);
                
                gbc.gridy = 3;
                JLabel anneeLabel = new JLabel("Année " + anneeActuelle);
                anneeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                centerPanel.add(anneeLabel, gbc);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Statut non disponible");
            errorLabel.setFont(new Font("Arial", Font.BOLD, 24));
            centerPanel.add(errorLabel);
        }
        
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
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
        new ClientEtudiant().setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ClientEtudiant().setVisible(true);
        });
    }
}