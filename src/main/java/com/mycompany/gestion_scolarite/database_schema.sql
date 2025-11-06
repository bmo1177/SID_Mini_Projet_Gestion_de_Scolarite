-- Base de données Gestion Scolarité
-- Basée sur le MCD de l'exercice 1

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
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des étudiants
CREATE TABLE etudiant (
    id_etudiant INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE,
    origine_scolaire ENUM('DUT', 'CPI', 'CPGE', 'BAC', 'AUTRE') NOT NULL,
    email VARCHAR(100),
    telephone VARCHAR(20),
    adresse TEXT,
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE SET NULL
);

-- Table des enseignants
CREATE TABLE enseignant (
    id_enseignant INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    specialite VARCHAR(100),
    email VARCHAR(100),
    telephone VARCHAR(20),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE SET NULL
);

-- Table des programmes (ING1 TC, ING2 TC, etc.)
CREATE TABLE programme (
    id_programme INT AUTO_INCREMENT PRIMARY KEY,
    code_programme VARCHAR(20) UNIQUE NOT NULL,
    libelle VARCHAR(200) NOT NULL,
    niveau INT NOT NULL, -- 1, 2, 3
    type_programme ENUM('TRONC_COMMUN', 'SPECIALITE', 'ORIENTATION', 'OPTION') NOT NULL,
    description TEXT,
    actif BOOLEAN DEFAULT TRUE
);

-- Table des prérequis entre programmes
CREATE TABLE prerequis_programme (
    id_programme INT,
    id_programme_requis INT,
    obligatoire BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (id_programme, id_programme_requis),
    FOREIGN KEY (id_programme) REFERENCES programme(id_programme) ON DELETE CASCADE,
    FOREIGN KEY (id_programme_requis) REFERENCES programme(id_programme_requis) ON DELETE CASCADE
);

-- Table des matières
CREATE TABLE matiere (
    id_matiere INT AUTO_INCREMENT PRIMARY KEY,
    code_matiere VARCHAR(20) UNIQUE NOT NULL,
    nom_matiere VARCHAR(200) NOT NULL,
    objectif TEXT,
    actif BOOLEAN DEFAULT TRUE
);

-- Table association programme-matière avec pondération et semestre
CREATE TABLE programme_matiere (
    id_programme INT,
    id_matiere INT,
    annee_scolaire VARCHAR(9) NOT NULL, -- Format: 2024-2025
    semestre INT NOT NULL, -- 1 ou 2
    ponderation DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    coefficient DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    PRIMARY KEY (id_programme, id_matiere, annee_scolaire),
    FOREIGN KEY (id_programme) REFERENCES programme(id_programme) ON DELETE CASCADE,
    FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere) ON DELETE CASCADE
);

-- Table des inscriptions des étudiants
CREATE TABLE inscription (
    id_inscription INT AUTO_INCREMENT PRIMARY KEY,
    id_etudiant INT NOT NULL,
    id_programme INT NOT NULL,
    annee_scolaire VARCHAR(9) NOT NULL,
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut_inscription ENUM('ACTIVE', 'ANNULEE', 'TERMINEE') DEFAULT 'ACTIVE',
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
    type_epreuve ENUM('CONTROLE', 'EXAMEN', 'PROJET', 'TP', 'TD', 'AUTRE') NOT NULL,
    libelle VARCHAR(200) NOT NULL,
    coefficient DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    date_epreuve DATE,
    duree INT, -- en minutes
    note_sur DECIMAL(5,2) DEFAULT 20.0,
    description TEXT,
    FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere) ON DELETE CASCADE,
    FOREIGN KEY (id_enseignant) REFERENCES enseignant(id_enseignant) ON DELETE SET NULL
);

-- Table des notes des étudiants aux épreuves
CREATE TABLE note_epreuve (
    id_note INT AUTO_INCREMENT PRIMARY KEY,
    id_etudiant INT NOT NULL,
    id_epreuve INT NOT NULL,
    note DECIMAL(5,2),
    absent BOOLEAN DEFAULT FALSE,
    date_saisie TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant) ON DELETE CASCADE,
    FOREIGN KEY (id_epreuve) REFERENCES epreuve(id_epreuve) ON DELETE CASCADE,
    UNIQUE KEY unique_note (id_etudiant, id_epreuve)
);

-- Table des notes finales par matière
CREATE TABLE note_matiere (
    id_note_matiere INT AUTO_INCREMENT PRIMARY KEY,
    id_etudiant INT NOT NULL,
    id_matiere INT NOT NULL,
    id_programme INT NOT NULL,
    annee_scolaire VARCHAR(9) NOT NULL,
    note_finale DECIMAL(5,2),
    validee BOOLEAN DEFAULT FALSE,
    date_validation TIMESTAMP NULL,
    FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant) ON DELETE CASCADE,
    FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere) ON DELETE CASCADE,
    FOREIGN KEY (id_programme) REFERENCES programme(id_programme) ON DELETE CASCADE,
    UNIQUE KEY unique_note_matiere (id_etudiant, id_matiere, id_programme, annee_scolaire)
);

-- Table des résultats annuels
CREATE TABLE resultat_annuel (
    id_resultat INT AUTO_INCREMENT PRIMARY KEY,
    id_etudiant INT NOT NULL,
    annee_scolaire VARCHAR(9) NOT NULL,
    moyenne_generale DECIMAL(5,2),
    statut_annuel ENUM('ADMIS', 'REDOUBLANT', 'EXCLU') NOT NULL,
    date_calcul TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valide BOOLEAN DEFAULT FALSE,
    commentaire TEXT,
    FOREIGN KEY (id_etudiant) REFERENCES etudiant(id_etudiant) ON DELETE CASCADE,
    UNIQUE KEY unique_resultat (id_etudiant, annee_scolaire)
);

-- Table des projets (optionnelle pour gérer les projets multi-matières)
CREATE TABLE projet (
    id_projet INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    annee_scolaire VARCHAR(9) NOT NULL,
    date_debut DATE,
    date_fin DATE
);

-- Table association projet-matière
CREATE TABLE projet_matiere (
    id_projet INT,
    id_matiere INT,
    PRIMARY KEY (id_projet, id_matiere),
    FOREIGN KEY (id_projet) REFERENCES projet(id_projet) ON DELETE CASCADE,
    FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere) ON DELETE CASCADE
);

-- Table des logs d'activité (audit)
CREATE TABLE log_activite (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT,
    action VARCHAR(100) NOT NULL,
    table_affectee VARCHAR(50),
    id_enregistrement INT,
    details TEXT,
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE SET NULL
);

-- Index pour améliorer les performances
CREATE INDEX idx_etudiant_nom ON etudiant(nom, prenom);
CREATE INDEX idx_inscription_annee ON inscription(annee_scolaire);
CREATE INDEX idx_note_epreuve_etudiant ON note_epreuve(id_etudiant);
CREATE INDEX idx_epreuve_matiere ON epreuve(id_matiere, annee_scolaire);
CREATE INDEX idx_resultat_annee ON resultat_annuel(annee_scolaire);

-- Insertion de données de test

-- Utilisateurs
INSERT INTO utilisateur (login, mot_de_passe, type_utilisateur) VALUES
('admin', SHA2('admin123', 256), 'ADMIN'),
('scolarite1', SHA2('scol123', 256), 'SCOLARITE'),
('direction1', SHA2('dir123', 256), 'DIRECTION'),
('prof.dupont', SHA2('prof123', 256), 'ENSEIGNANT'),
('prof.martin', SHA2('prof123', 256), 'ENSEIGNANT'),
('etud.ahmed', SHA2('etud123', 256), 'ETUDIANT'),
('etud.sarah', SHA2('etud123', 256), 'ETUDIANT'),
('etud.karim', SHA2('etud123', 256), 'ETUDIANT');

-- Enseignants
INSERT INTO enseignant (id_utilisateur, nom, prenom, specialite, email) VALUES
(4, 'Dupont', 'Jean', 'Informatique', 'jean.dupont@eisti.fr'),
(5, 'Martin', 'Marie', 'Mathématiques', 'marie.martin@eisti.fr');

-- Étudiants
INSERT INTO etudiant (id_utilisateur, nom, prenom, date_naissance, origine_scolaire, email) VALUES
(6, 'Benali', 'Ahmed', '2003-05-15', 'CPGE', 'ahmed.benali@eisti.fr'),
(7, 'Mansour', 'Sarah', '2003-08-22', 'DUT', 'sarah.mansour@eisti.fr'),
(8, 'Zoubir', 'Karim', '2003-03-10', 'CPI', 'karim.zoubir@eisti.fr');

-- Programmes
INSERT INTO programme (code_programme, libelle, niveau, type_programme, description) VALUES
('ING1_TC', 'Ingénieur 1ère année - Tronc Commun', 1, 'TRONC_COMMUN', 'Programme de première année'),
('ING2_TC', 'Ingénieur 2ème année - Tronc Commun', 2, 'TRONC_COMMUN', 'Programme de deuxième année'),
('ING2_GI', 'Spécialité Génie Informatique', 2, 'SPECIALITE', 'Spécialisation en informatique'),
('ING2_MSI', 'Orientation MSI', 2, 'ORIENTATION', 'Modélisation et Simulation Informatiques'),
('ING3_GL', 'Option Génie Logiciel', 3, 'OPTION', 'Spécialisation en génie logiciel'),
('ING3_ISIN', 'Option ISIN', 3, 'OPTION', 'Ingénierie des Systèmes d\'Information et Réseaux');

-- Prérequis
INSERT INTO prerequis_programme (id_programme, id_programme_requis, obligatoire) VALUES
(2, 1, TRUE), -- ING2_TC nécessite ING1_TC
(3, 2, TRUE), -- ING2_GI nécessite ING2_TC
(4, 3, TRUE), -- ING2_MSI nécessite ING2_GI
(5, 2, TRUE), -- ING3_GL nécessite ING2_TC
(6, 2, TRUE); -- ING3_ISIN nécessite ING2_TC

-- Matières
INSERT INTO matiere (code_matiere, nom_matiere, objectif) VALUES
('ALG01', 'Algorithmique', 'Maîtriser les algorithmes fondamentaux'),
('BDD01', 'Bases de Données', 'Comprendre la conception et l\'interrogation de BD'),
('PROG01', 'Programmation Java', 'Développer en POO avec Java'),
('MATH01', 'Mathématiques pour l\'ingénieur', 'Acquérir les bases mathématiques'),
('WEB01', 'Développement Web', 'Créer des applications web modernes'),
('SYS01', 'Systèmes d\'exploitation', 'Comprendre les OS'),
('RES01', 'Réseaux informatiques', 'Maîtriser les protocoles réseaux'),
('IA01', 'Intelligence Artificielle', 'Introduction à l\'IA et Machine Learning');

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
INSERT INTO inscription (id_etudiant, id_programme, annee_scolaire) VALUES
(1, 1, '2025-2026'), -- Ahmed en ING1_TC
(2, 1, '2025-2026'), -- Sarah en ING1_TC
(3, 2, '2025-2026'); -- Karim en ING2_TC

-- Épreuves
INSERT INTO epreuve (id_matiere, id_enseignant, annee_scolaire, type_epreuve, libelle, coefficient, date_epreuve, note_sur) VALUES
(1, 1, '2025-2026', 'CONTROLE', 'Contrôle Continu 1', 0.3, '2025-10-15', 20.0),
(1, 1, '2025-2026', 'EXAMEN', 'Examen Final', 0.5, '2025-12-20', 20.0),
(1, 1, '2025-2026', 'TP', 'TP Algorithmique', 0.2, '2025-11-10', 20.0),
(3, 1, '2025-2026', 'PROJET', 'Projet Java', 0.4, '2025-12-15', 20.0),
(3, 1, '2025-2026', 'EXAMEN', 'Examen Java', 0.6, '2025-12-18', 20.0),
(4, 2, '2025-2026', 'CONTROLE', 'Contrôle Maths', 0.4, '2025-10-20', 20.0),
(4, 2, '2025-2026', 'EXAMEN', 'Examen Maths', 0.6, '2025-12-22', 20.0);

-- Notes
INSERT INTO note_epreuve (id_etudiant, id_epreuve, note, absent) VALUES
-- Ahmed
(1, 1, 15.5, FALSE),
(1, 2, 14.0, FALSE),
(1, 3, 16.0, FALSE),
(1, 4, 17.0, FALSE),
(1, 5, 15.0, FALSE),
(1, 6, 13.5, FALSE),
(1, 7, 14.5, FALSE),
-- Sarah
(2, 1, 17.0, FALSE),
(2, 2, 16.5, FALSE),
(2, 3, 18.0, FALSE),
(2, 4, 16.0, FALSE),
(2, 5, 17.5, FALSE),
(2, 6, 15.0, FALSE),
(2, 7, 16.0, FALSE);

-- Vue pour faciliter les requêtes
CREATE VIEW v_notes_etudiants AS
SELECT 
    e.id_etudiant,
    e.nom,
    e.prenom,
    m.nom_matiere,
    ep.libelle AS epreuve,
    ep.type_epreuve,
    ep.coefficient,
    ne.note,
    ne.absent,
    ep.annee_scolaire
FROM etudiant e
JOIN note_epreuve ne ON e.id_etudiant = ne.id_etudiant
JOIN epreuve ep ON ne.id_epreuve = ep.id_epreuve
JOIN matiere m ON ep.id_matiere = m.id_matiere;

CREATE VIEW v_inscriptions_detaillees AS
SELECT 
    e.id_etudiant,
    e.nom,
    e.prenom,
    p.code_programme,
    p.libelle AS programme,
    i.annee_scolaire,
    i.statut_inscription
FROM etudiant e
JOIN inscription i ON e.id_etudiant = i.id_etudiant
JOIN programme p ON i.id_programme = p.id_programme;