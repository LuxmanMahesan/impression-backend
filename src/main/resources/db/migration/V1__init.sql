-- Nettoyage de l'ancien modèle si tu n'en as pas besoin
DROP TABLE IF EXISTS fichier_commande;
DROP TABLE IF EXISTS commande_impression;

-- Code journalier (une seule ligne id=1)
CREATE TABLE IF NOT EXISTS code_depot (
                                          id SMALLINT PRIMARY KEY,
                                          valeur VARCHAR(20) NOT NULL,
    date_generation DATE NOT NULL
    );

INSERT INTO code_depot (id, valeur, date_generation)
VALUES (1, '000000', CURRENT_DATE)
    ON CONFLICT (id) DO NOTHING;

-- Dépôt
CREATE TABLE IF NOT EXISTS depot (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON',
    cree_le TIMESTAMPTZ NOT NULL DEFAULT now(),
    valide_le TIMESTAMPTZ
    );

CREATE INDEX IF NOT EXISTS idx_depot_cree_le ON depot(cree_le);
CREATE INDEX IF NOT EXISTS idx_depot_statut ON depot(statut);

-- Fichiers d'un dépôt
CREATE TABLE IF NOT EXISTS fichier_depot (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    depot_id UUID NOT NULL REFERENCES depot(id) ON DELETE CASCADE,

    nom_original VARCHAR(255) NOT NULL,
    type_mime VARCHAR(120) NOT NULL,
    taille BIGINT NOT NULL,

    chemin_stockage TEXT NOT NULL,
    empreinte_sha256 VARCHAR(64),

    cree_le TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_fichier_depot_depot_id ON fichier_depot(depot_id);
