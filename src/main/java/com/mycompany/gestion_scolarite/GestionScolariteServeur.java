/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestion_scolarite;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDate;


public class GestionScolariteServeur {
    private static final int PORT = 5555;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/scolarite";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private static ExecutorService threadPool = Executors.newFixedThreadPool(50);
    private static Map<String, ClientHandler> clientsConnectes = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        System.out.println("=== Serveur de Gestion Scolarité ===");
        System.out.println("Démarrage du serveur sur le port " + PORT + "...");
        
        // Test de connexion à la BD
        if (!testerConnexionBD()) {
            System.err.println("ERREUR: Impossible de se connecter à la base de données!");
            return;
        }
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré avec succès!");
            System.out.println("En attente de connexions...\n");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion depuis: " + 
                    clientSocket.getInetAddress().getHostAddress());
                
                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
    
    private static boolean testerConnexionBD() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("✓ Connexion à la base de données réussie!");
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Échec de connexion à la BD: " + e.getMessage());
            return false;
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String login;
        private String typeUtilisateur;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                String requete;
                while ((requete = in.readLine()) != null) {
                    System.out.println("Requête reçue: " + requete);
                    String reponse = traiterRequete(requete);
                    out.println(reponse);
                }
            } catch (IOException e) {
                System.err.println("Erreur avec le client: " + e.getMessage());
            } finally {
                deconnecter();
            }
        }
        
        private String traiterRequete(String requete) {
            String[] parts = requete.split("\\|");
            String commande = parts[0];
            
            try {
                switch (commande) {
                    case "AUTH":
                        return authentifier(parts[1], parts[2]);
                    case "GET_INFO_ETUDIANT":
                        return getInfoEtudiant(parts[1]);
                    case "GET_NOTES_ETUDIANT":
                        return getNotesEtudiant(parts[1], parts.length > 2 ? parts[2] : null);
                    case "GET_MOYENNE_GENERALE":
                        return getMoyenneGenerale(parts[1], parts[2]);
                    case "GET_STATUT_ANNUEL":
                        return getStatutAnnuel(parts[1], parts[2]);
                    case "CREATE_EPREUVE":
                        return creerEpreuve(parts);
                    case "SAISIR_NOTES":
                        return saisirNotes(parts);
                    case "MODIFIER_EPREUVE":
                        return modifierEpreuve(parts);
                    case "SUPPRIMER_EPREUVE":
                        return supprimerEpreuve(parts[1]);
                    case "GET_RESULTATS_MATIERE":
                        return getResultatsMatiere(parts[1], parts[2]);
                    case "CALCULER_NOTE_MATIERE":
                        return calculerNoteMatiere(parts[1], parts[2], parts[3]);
                    case "INSCRIRE_ETUDIANT":
                        return inscrireEtudiant(parts);
                    case "GET_ETUDIANTS":
                        return getListeEtudiants();
                    case "GET_PROGRAMMES":
                        return getListeProgrammes();
                    case "GET_MATIERES":
                        return getListeMatieres(parts.length > 1 ? parts[1] : null);
                    case "GET_EPREUVES":
                        return getListeEpreuves(parts.length > 1 ? parts[1] : null);
                    case "CREER_PROGRAMME":
                        return creerProgramme(parts);
                    case "MODIFIER_PROGRAMME":
                        return modifierProgramme(parts);
                    case "CALCULER_MOYENNES":
                        return calculerMoyennesAutomatique(parts[1]);
                    case "GENERER_BULLETIN":
                        return genererBulletin(parts[1], parts[2]);
                    case "GET_STATISTIQUES":
                        return getStatistiques(parts.length > 1 ? parts[1] : null);
                    default:
                        return "ERROR|Commande inconnue: " + commande;
                }
            } catch (Exception e) {
                return "ERROR|Erreur serveur: " + e.getMessage();
            }
        }
        
        private String authentifier(String login, String mdp) {
            try (Connection conn = getConnection()) {
                String sql = "SELECT u.id_utilisateur, u.type_utilisateur, " +
                           "CASE " +
                           "  WHEN u.type_utilisateur = 'ETUDIANT' THEN e.id_etudiant " +
                           "  WHEN u.type_utilisateur = 'ENSEIGNANT' THEN ens.id_enseignant " +
                           "  ELSE NULL " +
                           "END as id_personne, " +
                           "CASE " +
                           "  WHEN u.type_utilisateur = 'ETUDIANT' THEN CONCAT(e.nom, ' ', e.prenom) " +
                           "  WHEN u.type_utilisateur = 'ENSEIGNANT' THEN CONCAT(ens.nom, ' ', ens.prenom) " +
                           "  ELSE u.login " +
                           "END as nom_complet " +
                           "FROM utilisateur u " +
                           "LEFT JOIN etudiant e ON u.id_utilisateur = e.id_utilisateur " +
                           "LEFT JOIN enseignant ens ON u.id_utilisateur = ens.id_utilisateur " +
                           "WHERE u.login = ? AND u.mot_de_passe = SHA2(?, 256) AND u.actif = TRUE";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, login);
                stmt.setString(2, mdp);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    this.login = login;
                    this.typeUtilisateur = rs.getString("type_utilisateur");
                    int idPersonne = rs.getInt("id_personne");
                    String nomComplet = rs.getString("nom_complet");
                    
                    clientsConnectes.put(login, this);
                    System.out.println("✓ Authentification réussie: " + login + " (" + typeUtilisateur + ")");
                    
                    return "SUCCESS|" + typeUtilisateur + "|" + idPersonne + "|" + nomComplet;
                } else {
                    return "ERROR|Identifiants incorrects";
                }
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String getInfoEtudiant(String idEtudiant) {
            try (Connection conn = getConnection()) {
                String sql = "SELECT e.*, u.login, u.actif " +
                           "FROM etudiant e " +
                           "LEFT JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur " +
                           "WHERE e.id_etudiant = ?";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(idEtudiant));
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    StringBuilder info = new StringBuilder("SUCCESS");
                    info.append("|").append(rs.getString("nom"));
                    info.append("|").append(rs.getString("prenom"));
                    info.append("|").append(rs.getDate("date_naissance"));
                    info.append("|").append(rs.getString("origine_scolaire"));
                    info.append("|").append(rs.getString("email"));
                    info.append("|").append(rs.getString("telephone"));
                    info.append("|").append(rs.getString("login"));
                    return info.toString();
                }
                return "ERROR|Étudiant introuvable";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String getNotesEtudiant(String idEtudiant, String annee) {
            try (Connection conn = getConnection()) {
                String sql = "SELECT m.nom_matiere, ep.libelle, ep.type_epreuve, " +
                           "ne.note, ne.absent, ep.coefficient, ep.date_epreuve, " +
                           "ep.annee_scolaire " +
                           "FROM note_epreuve ne " +
                           "JOIN epreuve ep ON ne.id_epreuve = ep.id_epreuve " +
                           "JOIN matiere m ON ep.id_matiere = m.id_matiere " +
                           "WHERE ne.id_etudiant = ? ";
                
                if (annee != null && !annee.isEmpty()) {
                    sql += "AND ep.annee_scolaire = ? ";
                }
                sql += "ORDER BY m.nom_matiere, ep.date_epreuve";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(idEtudiant));
                if (annee != null && !annee.isEmpty()) {
                    stmt.setString(2, annee);
                }
                
                ResultSet rs = stmt.executeQuery();
                StringBuilder result = new StringBuilder("SUCCESS");
                int count = 0;
                while (rs.next()) {
                    result.append("|").append(rs.getString("nom_matiere"));
                    result.append(";").append(rs.getString("libelle"));
                    result.append(";").append(rs.getString("type_epreuve"));
                    result.append(";").append(rs.getBoolean("absent") ? "ABS" : rs.getString("note"));
                    result.append(";").append(rs.getString("coefficient"));
                    result.append(";").append(rs.getDate("date_epreuve"));
                    count++;
                }
                return count > 0 ? result.toString() : "SUCCESS|Aucune note disponible";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String getMoyenneGenerale(String idEtudiant, String annee) {
            try (Connection conn = getConnection()) {
                // Calculer d'abord les notes de matières si nécessaire
                calculerToutesNotesMatiere(conn, idEtudiant, annee);
                
                String sql = "SELECT " +
                           "SUM(nm.note_finale * pm.ponderation) / SUM(pm.ponderation) as moyenne " +
                           "FROM note_matiere nm " +
                           "JOIN programme_matiere pm ON nm.id_matiere = pm.id_matiere " +
                           "  AND nm.id_programme = pm.id_programme " +
                           "  AND nm.annee_scolaire = pm.annee_scolaire " +
                           "WHERE nm.id_etudiant = ? AND nm.annee_scolaire = ?";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(idEtudiant));
                stmt.setString(2, annee);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double moyenne = rs.getDouble("moyenne");
                    return "SUCCESS|" + String.format("%.2f", moyenne);
                }
                return "ERROR|Impossible de calculer la moyenne";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String getStatutAnnuel(String idEtudiant, String annee) {
            try (Connection conn = getConnection()) {
                String sql = "SELECT moyenne_generale, statut_annuel, commentaire " +
                           "FROM resultat_annuel " +
                           "WHERE id_etudiant = ? AND annee_scolaire = ?";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(idEtudiant));
                stmt.setString(2, annee);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return "SUCCESS|" + rs.getDouble("moyenne_generale") + "|" +
                           rs.getString("statut_annuel") + "|" +
                           (rs.getString("commentaire") != null ? rs.getString("commentaire") : "");
                }
                
                // Si pas de résultat, le calculer
                String moyenneResp = getMoyenneGenerale(idEtudiant, annee);
                if (moyenneResp.startsWith("SUCCESS")) {
                    double moyenne = Double.parseDouble(moyenneResp.split("\\|")[1]);
                    String statut = moyenne >= 10 ? "ADMIS" : (moyenne >= 8 ? "REDOUBLANT" : "EXCLU");
                    
                    // Enregistrer le résultat
                    String insertSql = "INSERT INTO resultat_annuel " +
                                     "(id_etudiant, annee_scolaire, moyenne_generale, statut_annuel) " +
                                     "VALUES (?, ?, ?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                    insertStmt.setInt(1, Integer.parseInt(idEtudiant));
                    insertStmt.setString(2, annee);
                    insertStmt.setDouble(3, moyenne);
                    insertStmt.setString(4, statut);
                    insertStmt.executeUpdate();
                    
                    return "SUCCESS|" + moyenne + "|" + statut + "|";
                }
                return "ERROR|Aucun résultat disponible";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String creerEpreuve(String[] parts) {
            try (Connection conn = getConnection()) {
                // parts: CREATE_EPREUVE|idMatiere|idEnseignant|annee|type|libelle|coef|date|noteSur|description
                String sql = "INSERT INTO epreuve (id_matiere, id_enseignant, annee_scolaire, " +
                           "type_epreuve, libelle, coefficient, date_epreuve, note_sur, description) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, Integer.parseInt(parts[1]));
                stmt.setInt(2, Integer.parseInt(parts[2]));
                stmt.setString(3, parts[3]);
                stmt.setString(4, parts[4]);
                stmt.setString(5, parts[5]);
                stmt.setDouble(6, Double.parseDouble(parts[6]));
//                stmt.setDate(7, Date.valueOf(parts[7]));
                stmt.setDouble(8, Double.parseDouble(parts[8]));
                stmt.setString(9, parts.length > 9 ? parts[9] : null);
                
                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        return "SUCCESS|Épreuve créée avec ID: " + rs.getInt(1);
                    }
                }
                return "ERROR|Échec de création";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String saisirNotes(String[] parts) {
            try (Connection conn = getConnection()) {
                // parts: SAISIR_NOTES|idEpreuve|idEtudiant1:note1:absent1|idEtudiant2:note2:absent2|...
                conn.setAutoCommit(false);
                
                String sql = "INSERT INTO note_epreuve (id_etudiant, id_epreuve, note, absent) " +
                           "VALUES (?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE note = VALUES(note), absent = VALUES(absent)";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                int idEpreuve = Integer.parseInt(parts[1]);
                int count = 0;
                
                for (int i = 2; i < parts.length; i++) {
                    String[] noteData = parts[i].split(":");
                    int idEtudiant = Integer.parseInt(noteData[0]);
                    boolean absent = Boolean.parseBoolean(noteData[2]);
                    
                    stmt.setInt(1, idEtudiant);
                    stmt.setInt(2, idEpreuve);
                    if (absent) {
                        stmt.setNull(3, Types.DECIMAL);
                    } else {
                        stmt.setDouble(3, Double.parseDouble(noteData[1]));
                    }
                    stmt.setBoolean(4, absent);
                    
                    stmt.addBatch();
                    count++;
                }
                
                stmt.executeBatch();
                conn.commit();
                
                return "SUCCESS|" + count + " notes saisies";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String modifierEpreuve(String[] parts) {
            try (Connection conn = getConnection()) {
                // parts: MODIFIER_EPREUVE|idEpreuve|libelle|coef|date|description
                String sql = "UPDATE epreuve SET libelle = ?, coefficient = ?, " +
                           "date_epreuve = ?, description = ? WHERE id_epreuve = ?";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, parts[2]);
                stmt.setDouble(2, Double.parseDouble(parts[3]));
//                stmt.setDate(3, Date.valueOf(parts[4]));
                stmt.setString(4, parts.length > 5 ? parts[5] : null);
                stmt.setInt(5, Integer.parseInt(parts[1]));
                
                int affected = stmt.executeUpdate();
                return affected > 0 ? "SUCCESS|Épreuve modifiée" : "ERROR|Épreuve introuvable";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String supprimerEpreuve(String idEpreuve) {
            try (Connection conn = getConnection()) {
                String sql = "DELETE FROM epreuve WHERE id_epreuve = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(idEpreuve));
                
                int affected = stmt.executeUpdate();
                return affected > 0 ? "SUCCESS|Épreuve supprimée" : "ERROR|Épreuve introuvable";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String getResultatsMatiere(String idMatiere, String annee) {
            try (Connection conn = getConnection()) {
                String sql = "SELECT e.id_etudiant, e.nom, e.prenom, nm.note_finale, " +
                           "COUNT(ne.id_note) as nb_notes " +
                           "FROM etudiant e " +
                           "JOIN inscription i ON e.id_etudiant = i.id_etudiant " +
                           "LEFT JOIN note_matiere nm ON e.id_etudiant = nm.id_etudiant " +
                           "  AND nm.id_matiere = ? AND nm.annee_scolaire = ? " +
                           "LEFT JOIN note_epreuve ne ON e.id_etudiant = ne.id_etudiant " +
                           "LEFT JOIN epreuve ep ON ne.id_epreuve = ep.id_epreuve " +
                           "  AND ep.id_matiere = ? AND ep.annee_scolaire = ? " +
                           "WHERE i.annee_scolaire = ? " +
                           "GROUP BY e.id_etudiant " +
                           "ORDER BY e.nom, e.prenom";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(idMatiere));
                stmt.setString(2, annee);
                stmt.setInt(3, Integer.parseInt(idMatiere));
                stmt.setString(4, annee);
                stmt.setString(5, annee);
                
                ResultSet rs = stmt.executeQuery();
                StringBuilder result = new StringBuilder("SUCCESS");
                while (rs.next()) {
                    result.append("|").append(rs.getInt("id_etudiant"));
                    result.append(";").append(rs.getString("nom"));
                    result.append(";").append(rs.getString("prenom"));
                    result.append(";").append(rs.getObject("note_finale") != null ? 
                        rs.getDouble("note_finale") : "N/A");
                    result.append(";").append(rs.getInt("nb_notes"));
                }
                return result.toString();
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String calculerNoteMatiere(String idEtudiant, String idMatiere, String annee) {
            try (Connection conn = getConnection()) {
                // Calculer la moyenne pondérée des épreuves
                String sql = "SELECT SUM(ne.note * ep.coefficient) / SUM(ep.coefficient) as moyenne " +
                           "FROM note_epreuve ne " +
                           "JOIN epreuve ep ON ne.id_epreuve = ep.id_epreuve " +
                           "WHERE ne.id_etudiant = ? AND ep.id_matiere = ? " +
                           "AND ep.annee_scolaire = ? AND ne.absent = FALSE";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(idEtudiant));
                stmt.setInt(2, Integer.parseInt(idMatiere));
                stmt.setString(3, annee);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getObject("moyenne") != null) {
                    double moyenne = rs.getDouble("moyenne");
                    
                    // Récupérer le programme de l'étudiant
                    String progSql = "SELECT id_programme FROM inscription " +
                                   "WHERE id_etudiant = ? AND annee_scolaire = ? LIMIT 1";
                    PreparedStatement progStmt = conn.prepareStatement(progSql);
                    progStmt.setInt(1, Integer.parseInt(idEtudiant));
                    progStmt.setString(2, annee);
                    ResultSet progRs = progStmt.executeQuery();
                    
                    if (progRs.next()) {
                        int idProgramme = progRs.getInt("id_programme");
                        
                        // Enregistrer la note
                        String insertSql = "INSERT INTO note_matiere " +
                                         "(id_etudiant, id_matiere, id_programme, annee_scolaire, note_finale) " +
                                         "VALUES (?, ?, ?, ?, ?) " +
                                         "ON DUPLICATE KEY UPDATE note_finale = VALUES(note_finale)";
                        PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                        insertStmt.setInt(1, Integer.parseInt(idEtudiant));
                        insertStmt.setInt(2, Integer.parseInt(idMatiere));
                        insertStmt.setInt(3, idProgramme);
                        insertStmt.setString(4, annee);
                        insertStmt.setDouble(5, moyenne);
                        insertStmt.executeUpdate();
                        
                        return "SUCCESS|" + String.format("%.2f", moyenne);
                    }
                }
                return "ERROR|Impossible de calculer la note";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private void calculerToutesNotesMatiere(Connection conn, String idEtudiant, String annee) throws SQLException {
            String sql = "SELECT DISTINCT ep.id_matiere FROM epreuve ep " +
                       "JOIN note_epreuve ne ON ep.id_epreuve = ne.id_epreuve " +
                       "WHERE ne.id_etudiant = ? AND ep.annee_scolaire = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(idEtudiant));
            stmt.setString(2, annee);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                calculerNoteMatiere(idEtudiant, String.valueOf(rs.getInt("id_matiere")), annee);
            }
        }
        
        private String inscrireEtudiant(String[] parts) {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                
                // parts: INSCRIRE_ETUDIANT|nom|prenom|dateNaissance|origine|email|tel|login|mdp|idProgramme|annee
                
                // 1. Créer l'utilisateur
                String userSql = "INSERT INTO utilisateur (login, mot_de_passe, type_utilisateur) " +
                               "VALUES (?, SHA2(?, 256), 'ETUDIANT')";
                PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                userStmt.setString(1, parts[7]);
                userStmt.setString(2, parts[8]);
                userStmt.executeUpdate();
                
                ResultSet userRs = userStmt.getGeneratedKeys();
                int idUtilisateur = 0;
                if (userRs.next()) {
                    idUtilisateur = userRs.getInt(1);
                }
                
                // 2. Créer l'étudiant
                String etudSql = "INSERT INTO etudiant (id_utilisateur, nom, prenom, date_naissance, " +
                               "origine_scolaire, email, telephone) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement etudStmt = conn.prepareStatement(etudSql, Statement.RETURN_GENERATED_KEYS);
                etudStmt.setInt(1, idUtilisateur);
                etudStmt.setString(2, parts[1]);
                etudStmt.setString(3, parts[2]);
//                etudStmt.setDate(4, Date.valueOf(parts[3]));
                etudStmt.setString(5, parts[4]);
                etudStmt.setString(6, parts[5]);
                etudStmt.setString(7, parts[6]);
                etudStmt.executeUpdate();
                
                ResultSet etudRs = etudStmt.getGeneratedKeys();
                int idEtudiant = 0;
                if (etudRs.next()) {
                    idEtudiant = etudRs.getInt(1);
                }
                
                // 3. Créer l'inscription
                String inscSql = "INSERT INTO inscription (id_etudiant, id_programme, annee_scolaire) " +
                               "VALUES (?, ?, ?)";
                PreparedStatement inscStmt = conn.prepareStatement(inscSql);
                inscStmt.setInt(1, idEtudiant);
                inscStmt.setInt(2, Integer.parseInt(parts[9]));
                inscStmt.setString(3, parts[10]);
                inscStmt.executeUpdate();
                
                conn.commit();
                return "SUCCESS|Étudiant inscrit avec ID: " + idEtudiant;
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String getListeEtudiants() {
            try (Connection conn = getConnection()) {
                String sql = "SELECT e.id_etudiant, e.nom, e.prenom, e.origine_scolaire, " +
                           "e.email, GROUP_CONCAT(DISTINCT p.code_programme) as programmes " +
                           "FROM etudiant e " +
                           "LEFT JOIN inscription i ON e.id_etudiant = i.id_etudiant " +
                           "LEFT JOIN programme p ON i.id_programme = p.id_programme " +
                           "GROUP BY e.id_etudiant " +
                           "ORDER BY e.nom, e.prenom";
                
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                StringBuilder result = new StringBuilder("SUCCESS");
                while (rs.next()) {
                    result.append("|").append(rs.getInt("id_etudiant"));
                    result.append(";").append(rs.getString("nom"));
                    result.append(";").append(rs.getString("prenom"));
                    result.append(";").append(rs.getString("origine_scolaire"));
                    result.append(";").append(rs.getString("email"));
                    result.append(";").append(rs.getString("programmes") != null ? 
                        rs.getString("programmes") : "Aucun");
                }
                return result.toString();
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String getListeProgrammes() {
            try (Connection conn = getConnection()) {
                String sql = "SELECT id_programme, code_programme, libelle, niveau, type_programme " +
                           "FROM programme WHERE actif = TRUE ORDER BY niveau, code_programme";
                
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                StringBuilder result = new StringBuilder("SUCCESS");
                while (rs.next()) {
                    result.append("|").append(rs.getInt("id_programme"));
                    result.append(";").append(rs.getString("code_programme"));
                    result.append(";").append(rs.getString("libelle"));
                    result.append(";").append(rs.getInt("niveau"));
                    result.append(";").append(rs.getString("type_programme"));
                }
                return result.toString();
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String getListeMatieres(String idProgramme) {
            try (Connection conn = getConnection()) {
                String sql;
                PreparedStatement stmt;
                
                if (idProgramme != null && !idProgramme.isEmpty()) {
                    sql = "SELECT m.id_matiere, m.code_matiere, m.nom_matiere, " +
                         "pm.semestre, pm.ponderation " +
                         "FROM matiere m " +
                         "JOIN programme_matiere pm ON m.id_matiere = pm.id_matiere " +
                         "WHERE pm.id_programme = ? AND m.actif = TRUE " +
                         "ORDER BY pm.semestre, m.nom_matiere";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, Integer.parseInt(idProgramme));
                } else {
                    sql = "SELECT id_matiere, code_matiere, nom_matiere, objectif " +
                         "FROM matiere WHERE actif = TRUE ORDER BY nom_matiere";
                    stmt = conn.prepareStatement(sql);
                }
                
                ResultSet rs = stmt.executeQuery();
                StringBuilder result = new StringBuilder("SUCCESS");
                while (rs.next()) {
                    result.append("|").append(rs.getInt("id_matiere"));
                    result.append(";").append(rs.getString("code_matiere"));
                    result.append(";").append(rs.getString("nom_matiere"));
                    if (idProgramme != null) {
                        result.append(";").append(rs.getInt("semestre"));
                        result.append(";").append(rs.getDouble("ponderation"));
                    }
                }
                return result.toString();
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String getListeEpreuves(String idMatiere) {
            try (Connection conn = getConnection()) {
                String sql = "SELECT e.id_epreuve, e.libelle, e.type_epreuve, e.coefficient, " +
                           "e.date_epreuve, e.annee_scolaire, m.nom_matiere, " +
                           "COUNT(ne.id_note) as nb_notes " +
                           "FROM epreuve e " +
                           "JOIN matiere m ON e.id_matiere = m.id_matiere " +
                           "LEFT JOIN note_epreuve ne ON e.id_epreuve = ne.id_epreuve ";
                
                if (idMatiere != null && !idMatiere.isEmpty()) {
                    sql += "WHERE e.id_matiere = ? ";
                }
                
                sql += "GROUP BY e.id_epreuve ORDER BY e.date_epreuve DESC";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                if (idMatiere != null && !idMatiere.isEmpty()) {
                    stmt.setInt(1, Integer.parseInt(idMatiere));
                }
                
                ResultSet rs = stmt.executeQuery();
                StringBuilder result = new StringBuilder("SUCCESS");
                while (rs.next()) {
                    result.append("|").append(rs.getInt("id_epreuve"));
                    result.append(";").append(rs.getString("nom_matiere"));
                    result.append(";").append(rs.getString("libelle"));
                    result.append(";").append(rs.getString("type_epreuve"));
                    result.append(";").append(rs.getDouble("coefficient"));
                    result.append(";").append(rs.getDate("date_epreuve"));
                    result.append(";").append(rs.getString("annee_scolaire"));
                    result.append(";").append(rs.getInt("nb_notes"));
                }
                return result.toString();
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String creerProgramme(String[] parts) {
            try (Connection conn = getConnection()) {
                // parts: CREER_PROGRAMME|code|libelle|niveau|type|description
                String sql = "INSERT INTO programme (code_programme, libelle, niveau, " +
                           "type_programme, description) VALUES (?, ?, ?, ?, ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, parts[1]);
                stmt.setString(2, parts[2]);
                stmt.setInt(3, Integer.parseInt(parts[3]));
                stmt.setString(4, parts[4]);
                stmt.setString(5, parts.length > 5 ? parts[5] : null);
                
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return "SUCCESS|Programme créé avec ID: " + rs.getInt(1);
                }
                return "ERROR|Échec de création";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String modifierProgramme(String[] parts) {
            try (Connection conn = getConnection()) {
                // parts: MODIFIER_PROGRAMME|id|code|libelle|description
                String sql = "UPDATE programme SET code_programme = ?, libelle = ?, " +
                           "description = ? WHERE id_programme = ?";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, parts[2]);
                stmt.setString(2, parts[3]);
                stmt.setString(3, parts.length > 4 ? parts[4] : null);
                stmt.setInt(4, Integer.parseInt(parts[1]));
                
                int affected = stmt.executeUpdate();
                return affected > 0 ? "SUCCESS|Programme modifié" : "ERROR|Programme introuvable";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String calculerMoyennesAutomatique(String annee) {
            try (Connection conn = getConnection()) {
                // Calculer les notes de matières pour tous les étudiants
                String sql = "SELECT DISTINCT i.id_etudiant FROM inscription i WHERE i.annee_scolaire = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, annee);
                ResultSet rs = stmt.executeQuery();
                
                int count = 0;
                while (rs.next()) {
                    String idEtudiant = String.valueOf(rs.getInt("id_etudiant"));
                    calculerToutesNotesMatiere(conn, idEtudiant, annee);
                    getMoyenneGenerale(idEtudiant, annee);
                    getStatutAnnuel(idEtudiant, annee);
                    count++;
                }
                
                return "SUCCESS|Moyennes calculées pour " + count + " étudiants";
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String genererBulletin(String idEtudiant, String annee) {
            try (Connection conn = getConnection()) {
                StringBuilder bulletin = new StringBuilder("SUCCESS|");
                
                // Info étudiant
                String infoSql = "SELECT nom, prenom FROM etudiant WHERE id_etudiant = ?";
                PreparedStatement infoStmt = conn.prepareStatement(infoSql);
                infoStmt.setInt(1, Integer.parseInt(idEtudiant));
                ResultSet infoRs = infoStmt.executeQuery();
                if (infoRs.next()) {
                    bulletin.append(infoRs.getString("nom")).append(" ");
                    bulletin.append(infoRs.getString("prenom")).append("|");
                }
                
                // Notes par matière
                String notesSql = "SELECT m.nom_matiere, nm.note_finale, pm.ponderation " +
                                "FROM note_matiere nm " +
                                "JOIN matiere m ON nm.id_matiere = m.id_matiere " +
                                "JOIN programme_matiere pm ON nm.id_matiere = pm.id_matiere " +
                                "WHERE nm.id_etudiant = ? AND nm.annee_scolaire = ? " +
                                "ORDER BY m.nom_matiere";
                PreparedStatement notesStmt = conn.prepareStatement(notesSql);
                notesStmt.setInt(1, Integer.parseInt(idEtudiant));
                notesStmt.setString(2, annee);
                ResultSet notesRs = notesStmt.executeQuery();
                
                while (notesRs.next()) {
                    bulletin.append(notesRs.getString("nom_matiere")).append(":");
                    bulletin.append(notesRs.getDouble("note_finale")).append(":");
                    bulletin.append(notesRs.getDouble("ponderation")).append(";");
                }
                
                // Moyenne et statut
                String statutResp = getStatutAnnuel(idEtudiant, annee);
                if (statutResp.startsWith("SUCCESS")) {
                    String[] statutParts = statutResp.split("\\|");
                    bulletin.append("|").append(statutParts[1]); // Moyenne
                    bulletin.append("|").append(statutParts[2]); // Statut
                }
                
                return bulletin.toString();
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private String getStatistiques(String annee) {
            try (Connection conn = getConnection()) {
                StringBuilder stats = new StringBuilder("SUCCESS|");
                
                // Nombre total d'étudiants
                String totalSql = "SELECT COUNT(DISTINCT id_etudiant) as total FROM inscription";
                if (annee != null) {
                    totalSql += " WHERE annee_scolaire = ?";
                }
                PreparedStatement totalStmt = conn.prepareStatement(totalSql);
                if (annee != null) {
                    totalStmt.setString(1, annee);
                }
                ResultSet totalRs = totalStmt.executeQuery();
                if (totalRs.next()) {
                    stats.append("Total:").append(totalRs.getInt("total")).append("|");
                }
                
                // Statistiques par statut
                if (annee != null) {
                    String statutSql = "SELECT statut_annuel, COUNT(*) as nb " +
                                     "FROM resultat_annuel WHERE annee_scolaire = ? " +
                                     "GROUP BY statut_annuel";
                    PreparedStatement statutStmt = conn.prepareStatement(statutSql);
                    statutStmt.setString(1, annee);
                    ResultSet statutRs = statutStmt.executeQuery();
                    
                    while (statutRs.next()) {
                        stats.append(statutRs.getString("statut_annuel")).append(":");
                        stats.append(statutRs.getInt("nb")).append(";");
                    }
                }
                
                return stats.toString();
            } catch (SQLException e) {
                return "ERROR|" + e.getMessage();
            }
        }
        
        private void deconnecter() {
            try {
                if (login != null) {
                    clientsConnectes.remove(login);
                    System.out.println("✗ Déconnexion: " + login);
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
            }
        }
    }
}
