-- Base de données Gestion Scolarité
-- Version améliorée avec corrections des problèmes identifiés

DROP DATABASE IF EXISTS scolarite;
CREATE DATABASE scolarite CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scolarite;

-- Table des utilisateurs (pour authentification)
CREATE TABLE utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    type_utilisateur ENUM('ETUDIANT', 'ENSEIGNANT', 'SCOLARITE', 'DIRECTION', 'ADMIN') NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des étudiants
CREATE TABLE etudiant (
    id_etudiant INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT UNIQUE,
    numero_etudiant VARCHAR(20) UNIQUE NOT NULL, -- Ajout d'un numéro étudiant unique
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE,
    origine_scolaire ENUM('DUT', 'CPI', 'CPGE', 'BAC', 'AUTRE') NOT NULL,
    email VARCHAR(100) UNIQUE,
    telephone VARCHAR(20),
    adresse TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE SET NULL
);

-- Table des enseignants
CREATE TABLE enseignant (
    id_enseignant INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT UNIQUE,
    numero_enseignant VARCHAR(20) UNIQUE NOT NULL, -- Ajout d'un numéro enseignant unique
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    specialite VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    telephone VARCHAR(20),
    bureau VARCHAR(50),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE SET NULL
);

-- Table des programmes (ING1 TC, ING2 TC, etc.)
CREATE TABLE programme (
    id_programme INT AUTO_INCREMENT PRIMARY KEY,
    code_programme VARCHAR(20) UNIQUE NOT NULL,
    libelle VARCHAR(200) NOT NULL,
    niveau INT NOT NULL CHECK (niveau BETWEEN 1 AND 5), -- 1, 2, 3, 4, 5
    type_programme ENUM('TRONC_COMMUN', 'SPECIALITE', 'ORIENTATION', 'OPTION') NOT NULL,
    credits_ects INT DEFAULT 0,
    description TEXT,
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des prérequis entre programmes (CORRIGÉE - évite l'auto-référence)
CREATE TABLE prerequis_programme (
    id_prerequis INT AUTO_INCREMENT PRIMARY KEY,
    id_programme INT NOT NULL,
    id_programme_requis INT NOT NULL,
    obligatoire BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_programme) REFERENCES programme(id_programme) ON DELETE CASCADE,
    FOREIGN KEY (id_programme_requis) REFERENCES programme(id_programme) ON DELETE CASCADE,
    UNIQUE KEY unique_prerequis (id_programme, id_programme_requis),
    CHECK (id_programme != id_programme_requis) -- Évite qu'un programme soit son propre prérequis
);

-- Table des matières
CREATE TABLE matiere (
    id_matiere INT AUTO_INCREMENT PRIMARY KEY,
    code_matiere VARCHAR(20) UNIQUE NOT NULL,
    nom_matiere VARCHAR(200) NOT NULL,
    objectif TEXT,
    credits_ects INT DEFAULT 0,
    volume_horaire INT, -- en heures
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table association programme-matière avec pondération et semestre
CREATE TABLE programme_matiere (
    id_programme_matiere INT AUTO_INCREMENT PRIMARY KEY,
    id_programme INT NOT NULL,
    id_matiere INT NOT NULL,
    annee_scolaire VARCHAR(9) NOT NULL, -- Format: 2024-2025
    semestre INT NOT NULL CHECK (semestre IN (1, 2)),
    ponderation DECIMAL(5,2) NOT NULL DEFAULT 1.0 CHECK (ponderation > 0),
    coefficient DECIMAL(5,2) NOT NULL DEFAULT 1.0 CHECK (coefficient > 0),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_programme) REFERENCES programme(id_programme) ON DELETE CASCADE,
    FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere) ON DELETE CASCADE,
    UNIQUE KEY unique_programme_matiere (id_programme, id_matiere, annee_scolaire)
);

-- Table des inscriptions des étudiants
CREATE TABLE inscription (
    id_inscription INT AUTO_INCREMENT PRIMARY KEY,
    id_etudiant INT NOT NULL,
    id_programme INT NOT NULL,
    annee_scolaire VARCHAR(9) NOT NULL,
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut_inscription ENUM('ACTIVE', 'ANNULEE', 'TERMINEE', 'SUSPENDUE') DEFAULT 'ACTIVE',
    frais_scolarite DECIMAL(10,2) DEFAULT 0,
    frais_payes DECIMAL(10,2) DEFAULT 0,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant) ON DELETE CASCADE,
    FOREIGN KEY (id_programme) REFERENCES programme(id_programme) ON DELETE CASCADE,
    UNIQUE KEY unique_inscription (id_etudiant, id_programme, annee_scolaire)
);

-- Table des épreuves
CREATE TABLE epreuve (
    id_epreuve INT AUTO_INCREMENT PRIMARY KEY,
    id_matiere INT NOT NULL,
    id_enseignant INT,
    annee_scolaire VARCHAR(9) NOT NULL,
    type_epreuve ENUM('CONTROLE', 'EXAMEN', 'PROJET', 'TP', 'TD', 'ORAL', 'AUTRE') NOT NULL,
    libelle VARCHAR(200) NOT NULL,
    coefficient DECIMAL(5,2) NOT NULL DEFAULT 1.0 CHECK (coefficient > 0),
    date_epreuve DATETIME, -- Changé en DATETIME pour inclure l'heure
    duree INT, -- en minutes
    note_sur DECIMAL(5,2) DEFAULT 20.0 CHECK (note_sur > 0),
    description TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere) ON DELETE CASCADE,
    FOREIGN KEY (id_enseignant) REFERENCES enseignant(id_enseignant) ON DELETE SET NULL
);

-- Table des notes des étudiants aux épreuves
CREATE TABLE note_epreuve (
    id_note INT AUTO_INCREMENT PRIMARY KEY,
    id_etudiant INT NOT NULL,
    id_epreuve INT NOT NULL,
    note DECIMAL(5,2) CHECK (note >= 0 AND note <= 20), -- Validation de la plage de notes
    absent BOOLEAN DEFAULT FALSE,
    date_saisie TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_enseignant_saisie INT, -- Qui a saisi la note
    commentaire TEXT,
    FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant) ON DELETE CASCADE,
    FOREIGN KEY (id_epreuve) REFERENCES epreuve(id_epreuve) ON DELETE CASCADE,
    FOREIGN KEY (id_enseignant_saisie) REFERENCES enseignant(id_enseignant) ON DELETE SET NULL,
    UNIQUE KEY unique_note (id_etudiant, id_epreuve),
    CHECK ((absent = TRUE AND note IS NULL) OR (absent = FALSE AND note IS NOT NULL))
);

-- Table des notes finales par matière
CREATE TABLE note_matiere (
    id_note_matiere INT AUTO_INCREMENT PRIMARY KEY,
    id_etudiant INT NOT NULL,
    id_matiere INT NOT NULL,
    id_programme INT NOT NULL,
    annee_scolaire VARCHAR(9) NOT NULL,
    note_finale DECIMAL(5,2) CHECK (note_finale >= 0 AND note_finale <= 20),
    validee BOOLEAN DEFAULT FALSE,
    date_validation TIMESTAMP NULL,
    id_enseignant_validation INT,
    appreciation TEXT,
    FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant) ON DELETE CASCADE,
    FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere) ON DELETE CASCADE,
    FOREIGN KEY (id_programme) REFERENCES programme(id_programme) ON DELETE CASCADE,
    FOREIGN KEY (id_enseignant_validation) REFERENCES enseignant(id_enseignant) ON DELETE SET NULL,
    UNIQUE KEY unique_note_matiere (id_etudiant, id_matiere, id_programme, annee_scolaire)
);

-- Table des résultats annuels
CREATE TABLE resultat_annuel (
    id_resultat INT AUTO_INCREMENT PRIMARY KEY,
    id_etudiant INT NOT NULL,
    annee_scolaire VARCHAR(9) NOT NULL,
    moyenne_generale DECIMAL(5,2) CHECK (moyenne_generale >= 0 AND moyenne_generale <= 20),
    credits_obtenus INT DEFAULT 0,
    statut_annuel ENUM('ADMIS', 'REDOUBLANT', 'EXCLU', 'ABANDON', 'TRANSFERT') NOT NULL,
    date_calcul TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valide BOOLEAN DEFAULT FALSE,
    id_enseignant_validation INT,
    commentaire TEXT,
    FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant) ON DELETE CASCADE,
    FOREIGN KEY (id_enseignant_validation) REFERENCES enseignant(id_enseignant) ON DELETE SET NULL,
    UNIQUE KEY unique_resultat (id_etudiant, annee_scolaire)
);

-- Table des projets (optionnelle pour gérer les projets multi-matières)
CREATE TABLE projet (
    id_projet INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    annee_scolaire VARCHAR(9) NOT NULL,
    date_debut DATE,
    date_fin DATE,
    id_enseignant_referent INT,
    FOREIGN KEY (id_enseignant_referent) REFERENCES enseignant(id_enseignant) ON DELETE SET NULL
);

-- Table association projet-matière
CREATE TABLE projet_matiere (
    id_projet_matiere INT AUTO_INCREMENT PRIMARY KEY,
    id_projet INT NOT NULL,
    id_matiere INT NOT NULL,
    coefficient DECIMAL(5,2) DEFAULT 1.0,
    FOREIGN KEY (id_projet) REFERENCES projet(id_projet) ON DELETE CASCADE,
    FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere) ON DELETE CASCADE,
    UNIQUE KEY unique_projet_matiere (id_projet, id_matiere)
);

-- Table association projet-étudiant
CREATE TABLE projet_etudiant (
    id_projet_etudiant INT AUTO_INCREMENT PRIMARY KEY,
    id_projet INT NOT NULL,
    id_etudiant INT NOT NULL,
    role_etudiant VARCHAR(100),
    note_projet DECIMAL(5,2) CHECK (note_projet >= 0 AND note_projet <= 20),
    FOREIGN KEY (id_projet) REFERENCES projet(id_projet) ON DELETE CASCADE,
    FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant) ON DELETE CASCADE,
    UNIQUE KEY unique_projet_etudiant (id_projet, id_etudiant)
);

-- Table des logs d'activité (audit)
CREATE TABLE log_activite (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT,
    action VARCHAR(100) NOT NULL,
    table_affectee VARCHAR(50),
    id_enregistrement INT,
    anciennes_valeurs JSON,
    nouvelles_valeurs JSON,
    details TEXT,
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE SET NULL
);

-- Index pour améliorer les performances
CREATE INDEX idx_etudiant_nom ON etudiant(nom, prenom);
CREATE INDEX idx_etudiant_numero ON etudiant(numero_etudiant);
CREATE INDEX idx_enseignant_nom ON enseignant(nom, prenom);
CREATE INDEX idx_inscription_annee ON inscription(annee_scolaire);
CREATE INDEX idx_inscription_etudiant ON inscription(id_etudiant);
CREATE INDEX idx_note_epreuve_etudiant ON note_epreuve(id_etudiant);
CREATE INDEX idx_epreuve_matiere ON epreuve(id_matiere, annee_scolaire);
CREATE INDEX idx_resultat_annee ON resultat_annuel(annee_scolaire);
CREATE INDEX idx_programme_matiere_annee ON programme_matiere(annee_scolaire);
CREATE INDEX idx_epreuve_date ON epreuve(date_epreuve);

-- Insertion de données de test améliorées

-- Utilisateurs
INSERT INTO utilisateur (login, mot_de_passe, type_utilisateur) VALUES
('admin', SHA2('admin123', 256), 'ADMIN'),
('scolarite1', SHA2('scol123', 256), 'SCOLARITE'),
('direction1', SHA2('dir123', 256), 'DIRECTION'),
('prof.dupont', SHA2('prof123', 256), 'ENSEIGNANT'),
('prof.martin', SHA2('prof123', 256), 'ENSEIGNANT'),
('prof.leroy', SHA2('prof123', 256), 'ENSEIGNANT'),
('etud.ahmed', SHA2('etud123', 256), 'ETUDIANT'),
('etud.sarah', SHA2('etud123', 256), 'ETUDIANT'),
('etud.karim', SHA2('etud123', 256), 'ETUDIANT'),
('etud.lucie', SHA2('etud123', 256), 'ETUDIANT');

-- Enseignants
INSERT INTO enseignant (id_utilisateur, numero_enseignant, nom, prenom, specialite, email, bureau) VALUES
(4, 'ENS001', 'Dupont', 'Jean', 'Informatique', 'jean.dupont@eisti.fr', 'Bureau A101'),
(5, 'ENS002', 'Martin', 'Marie', 'Mathématiques', 'marie.martin@eisti.fr', 'Bureau A102'),
(6, 'ENS003', 'Leroy', 'Pierre', 'Réseaux', 'pierre.leroy@eisti.fr', 'Bureau A103');

-- Étudiants
INSERT INTO etudiant (id_utilisateur, numero_etudiant, nom, prenom, date_naissance, origine_scolaire, email) VALUES
(7, 'ETU2025001', 'Benali', 'Ahmed', '2003-05-15', 'CPGE', 'ahmed.benali@eisti.fr'),
(8, 'ETU2025002', 'Mansour', 'Sarah', '2003-08-22', 'DUT', 'sarah.mansour@eisti.fr'),
(9, 'ETU2025003', 'Zoubir', 'Karim', '2003-03-10', 'CPI', 'karim.zoubir@eisti.fr'),
(10, 'ETU2025004', 'Dubois', 'Lucie', '2003-11-30', 'BAC', 'lucie.dubois@eisti.fr');

-- Programmes
INSERT INTO programme (code_programme, libelle, niveau, type_programme, credits_ects, description) VALUES
('ING1_TC', 'Ingénieur 1ère année - Tronc Commun', 1, 'TRONC_COMMUN', 60, 'Programme de première année - Tronc commun'),
('ING2_TC', 'Ingénieur 2ème année - Tronc Commun', 2, 'TRONC_COMMUN', 60, 'Programme de deuxième année - Tronc commun'),
('ING2_GI', 'Spécialité Génie Informatique', 2, 'SPECIALITE', 30, 'Spécialisation en informatique'),
('ING2_MSI', 'Orientation MSI', 2, 'ORIENTATION', 30, 'Modélisation et Simulation Informatiques'),
('ING3_GL', 'Option Génie Logiciel', 3, 'OPTION', 30, 'Spécialisation en génie logiciel'),
('ING3_ISIN', 'Option ISIN', 3, 'OPTION', 30, 'Ingénierie des Systèmes d\'Information et Réseaux');

-- Prérequis
INSERT INTO prerequis_programme (id_programme, id_programme_requis, obligatoire) VALUES
(2, 1, TRUE), -- ING2_TC nécessite ING1_TC
(3, 2, TRUE), -- ING2_GI nécessite ING2_TC
(4, 3, TRUE), -- ING2_MSI nécessite ING2_GI
(5, 2, TRUE), -- ING3_GL nécessite ING2_TC
(6, 2, TRUE); -- ING3_ISIN nécessite ING2_TC

-- Matières
INSERT INTO matiere (code_matiere, nom_matiere, objectif, credits_ects, volume_horaire) VALUES
('ALG01', 'Algorithmique', 'Maîtriser les algorithmes fondamentaux', 6, 60),
('BDD01', 'Bases de Données', 'Comprendre la conception et l\'interrogation de BD', 6, 60),
('PROG01', 'Programmation Java', 'Développer en POO avec Java', 6, 70),
('MATH01', 'Mathématiques pour l\'ingénieur', 'Acquérir les bases mathématiques', 6, 60),
('WEB01', 'Développement Web', 'Créer des applications web modernes', 6, 70),
('SYS01', 'Systèmes d\'exploitation', 'Comprendre les OS', 6, 60),
('RES01', 'Réseaux informatiques', 'Maîtriser les protocoles réseaux', 6, 60),
('IA01', 'Intelligence Artificielle', 'Introduction à l\'IA et Machine Learning', 6, 60);

-- Programme-Matière pour l'année 2025-2026
INSERT INTO programme_matiere (id_programme, id_matiere, annee_scolaire, semestre, ponderation, coefficient) VALUES
-- ING1_TC - Semestre 1
(1, 1, '2025-2026', 1, 2.0, 2.0), -- ALG01
(1, 4, '2025-2026', 1, 2.0, 2.0), -- MATH01
(1, 3, '2025-2026', 1, 3.0, 3.0), -- PROG01
-- ING1_TC - Semestre 2
(1, 2, '2025-2026', 2, 3.0, 3.0), -- BDD01
(1, 6, '2025-2026', 2, 2.0, 2.0), -- SYS01
-- ING2_TC
(2, 5, '2025-2026', 1, 3.0, 3.0), -- WEB01
(2, 7, '2025-2026', 1, 2.0, 2.0), -- RES01
-- ING3_GL
(5, 8, '2025-2026', 1, 4.0, 4.0); -- IA01

-- Inscriptions
INSERT INTO inscription (id_etudiant, id_programme, annee_scolaire, frais_scolarite, frais_payes) VALUES
(1, 1, '2025-2026', 5000.00, 5000.00), -- Ahmed en ING1_TC
(2, 1, '2025-2026', 5000.00, 2500.00), -- Sarah en ING1_TC
(3, 2, '2025-2026', 5500.00, 5500.00), -- Karim en ING2_TC
(4, 2, '2025-2026', 5500.00, 0.00);    -- Lucie en ING2_TC

-- Épreuves
INSERT INTO epreuve (id_matiere, id_enseignant, annee_scolaire, type_epreuve, libelle, coefficient, date_epreuve, note_sur) VALUES
(1, 1, '2025-2026', 'CONTROLE', 'Contrôle Continu 1', 0.3, '2025-10-15 14:00:00', 20.0),
(1, 1, '2025-2026', 'EXAMEN', 'Examen Final', 0.5, '2025-12-20 09:00:00', 20.0),
(1, 1, '2025-2026', 'TP', 'TP Algorithmique', 0.2, '2025-11-10 10:00:00', 20.0),
(3, 1, '2025-2026', 'PROJET', 'Projet Java', 0.4, '2025-12-15 23:59:59', 20.0),
(3, 1, '2025-2026', 'EXAMEN', 'Examen Java', 0.6, '2025-12-18 14:00:00', 20.0),
(4, 2, '2025-2026', 'CONTROLE', 'Contrôle Maths', 0.4, '2025-10-20 16:00:00', 20.0),
(4, 2, '2025-2026', 'EXAMEN', 'Examen Maths', 0.6, '2025-12-22 09:00:00', 20.0);

-- Notes
INSERT INTO note_epreuve (id_etudiant, id_epreuve, note, absent, id_enseignant_saisie) VALUES
-- Ahmed
(1, 1, 15.5, FALSE, 1),
(1, 2, 14.0, FALSE, 1),
(1, 3, 16.0, FALSE, 1),
(1, 4, 17.0, FALSE, 1),
(1, 5, 15.0, FALSE, 1),
(1, 6, 13.5, FALSE, 2),
(1, 7, 14.5, FALSE, 2),
-- Sarah
(2, 1, 17.0, FALSE, 1),
(2, 2, 16.5, FALSE, 1),
(2, 3, 18.0, FALSE, 1),
(2, 4, 16.0, FALSE, 1),
(2, 5, 17.5, FALSE, 1),
(2, 6, 15.0, FALSE, 2),
(2, 7, 16.0, FALSE, 2),
-- Karim
(3, 1, 12.0, FALSE, 1),
(3, 2, 11.5, FALSE, 1),
(3, 3, 14.0, FALSE, 1);

-- Vues améliorées

CREATE VIEW v_notes_etudiants AS
SELECT 
    e.id_etudiant,
    e.numero_etudiant,
    e.nom,
    e.prenom,
    p.code_programme,
    p.libelle AS programme,
    m.code_matiere,
    m.nom_matiere,
    ep.libelle AS epreuve,
    ep.type_epreuve,
    ep.coefficient AS coeff_epreuve,
    ne.note,
    ne.absent,
    ep.note_sur,
    ep.annee_scolaire,
    ens.nom AS nom_enseignant,
    ens.prenom AS prenom_enseignant
FROM etudiant e
JOIN inscription i ON e.id_etudiant = i.id_etudiant
JOIN programme p ON i.id_programme = p.id_programme
JOIN note_epreuve ne ON e.id_etudiant = ne.id_etudiant
JOIN epreuve ep ON ne.id_epreuve = ep.id_epreuve
JOIN matiere m ON ep.id_matiere = m.id_matiere
LEFT JOIN enseignant ens ON ep.id_enseignant = ens.id_enseignant
WHERE i.annee_scolaire = ep.annee_scolaire;

CREATE VIEW v_inscriptions_detaillees AS
SELECT 
    e.id_etudiant,
    e.numero_etudiant,
    e.nom,
    e.prenom,
    e.email,
    p.code_programme,
    p.libelle AS programme,
    p.niveau,
    i.annee_scolaire,
    i.statut_inscription,
    i.frais_scolarite,
    i.frais_payes,
    (i.frais_scolarite - i.frais_payes) AS reste_a_payer
FROM etudiant e
JOIN inscription i ON e.id_etudiant = i.id_etudiant
JOIN programme p ON i.id_programme = p.id_programme;

CREATE VIEW v_moyennes_etudiants AS
SELECT 
    e.id_etudiant,
    e.numero_etudiant,
    e.nom,
    e.prenom,
    p.code_programme,
    p.libelle AS programme,
    m.code_matiere,
    m.nom_matiere,
    pm.annee_scolaire,
    pm.semestre,
    AVG(ne.note) AS moyenne_matiere,
    COUNT(ne.id_note) AS nombre_epreuves
FROM etudiant e
JOIN inscription i ON e.id_etudiant = i.id_etudiant
JOIN programme p ON i.id_programme = p.id_programme
JOIN programme_matiere pm ON p.id_programme = pm.id_programme
JOIN matiere m ON pm.id_matiere = m.id_matiere
LEFT JOIN epreuve ep ON m.id_matiere = ep.id_matiere AND ep.annee_scolaire = pm.annee_scolaire
LEFT JOIN note_epreuve ne ON ep.id_epreuve = ne.id_epreuve AND ne.id_etudiant = e.id_etudiant
WHERE i.annee_scolaire = pm.annee_scolaire
GROUP BY e.id_etudiant, m.id_matiere, pm.annee_scolaire;

-- Procédures stockées utiles

DELIMITER //

CREATE PROCEDURE CalculerMoyenneMatiere(
    IN p_id_etudiant INT,
    IN p_id_matiere INT,
    IN p_annee_scolaire VARCHAR(9)
)
BEGIN
    DECLARE v_moyenne DECIMAL(5,2);
    DECLARE v_note_finale DECIMAL(5,2);
    
    SELECT AVG(ne.note * ep.coefficient) / SUM(ep.coefficient)
    INTO v_moyenne
    FROM note_epreuve ne
    JOIN epreuve ep ON ne.id_epreuve = ep.id_epreuve
    WHERE ne.id_etudiant = p_id_etudiant
    AND ep.id_matiere = p_id_matiere
    AND ep.annee_scolaire = p_annee_scolaire
    AND ne.absent = FALSE;
    
    SET v_note_finale = COALESCE(v_moyenne, 0);
    
    -- Insert or update the final grade
    INSERT INTO note_matiere (id_etudiant, id_matiere, id_programme, annee_scolaire, note_finale)
    SELECT p_id_etudiant, p_id_matiere, i.id_programme, p_annee_scolaire, v_note_finale
    FROM inscription i
    WHERE i.id_etudiant = p_id_etudiant
    AND i.annee_scolaire = p_annee_scolaire
    ON DUPLICATE KEY UPDATE 
        note_finale = v_note_finale,
        date_validation = CASE WHEN v_note_finale IS NOT NULL THEN CURRENT_TIMESTAMP ELSE NULL END;
    
END//

DELIMITER ;

-- Déclencheurs pour l'audit

DELIMITER //

CREATE TRIGGER audit_notes_epreuve_insert
AFTER INSERT ON note_epreuve
FOR EACH ROW
BEGIN
    INSERT INTO log_activite (id_utilisateur, action, table_affectee, id_enregistrement, nouvelles_valeurs)
    VALUES (
        (SELECT id_utilisateur FROM enseignant WHERE id_enseignant = NEW.id_enseignant_saisie),
        'INSERT',
        'note_epreuve',
        NEW.id_note,
        JSON_OBJECT('id_etudiant', NEW.id_etudiant, 'id_epreuve', NEW.id_epreuve, 'note', NEW.note, 'absent', NEW.absent)
    );
END//

CREATE TRIGGER audit_notes_epreuve_update
AFTER UPDATE ON note_epreuve
FOR EACH ROW
BEGIN
    INSERT INTO log_activite (id_utilisateur, action, table_affectee, id_enregistrement, anciennes_valeurs, nouvelles_valeurs)
    VALUES (
        (SELECT id_utilisateur FROM enseignant WHERE id_enseignant = NEW.id_enseignant_saisie),
        'UPDATE',
        'note_epreuve',
        NEW.id_note,
        JSON_OBJECT('id_etudiant', OLD.id_etudiant, 'id_epreuve', OLD.id_epreuve, 'note', OLD.note, 'absent', OLD.absent),
        JSON_OBJECT('id_etudiant', NEW.id_etudiant, 'id_epreuve', NEW.id_epreuve, 'note', NEW.note, 'absent', NEW.absent)
    );
END//

DELIMITER ;
