-- Ajout de la colonne contenu (bytea) pour stocker les fichiers en base
ALTER TABLE fichier_depot ADD COLUMN contenu BYTEA;

-- Rendre chemin_stockage nullable (on ne l'utilise plus)
ALTER TABLE fichier_depot ALTER COLUMN chemin_stockage DROP NOT NULL;