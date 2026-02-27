-- Ajout d'un identifiant court (5 caractères) lisible par le client
ALTER TABLE depot ADD COLUMN code_public VARCHAR(5);

-- Générer un code pour les dépôts existants (si jamais il y en a)
UPDATE depot SET code_public = UPPER(SUBSTR(REPLACE(id::text, '-', ''), 1, 5)) WHERE code_public IS NULL;

-- Rendre la colonne NOT NULL + unique
ALTER TABLE depot ALTER COLUMN code_public SET NOT NULL;
CREATE UNIQUE INDEX idx_depot_code_public ON depot(code_public);