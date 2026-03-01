import psycopg2
from faker import Faker
import random

# Connexion à la base de données MySql
conn = psycopg2.connect(
    dbname="dev_freelance",
    user="dev_freelance_user",
    password="freelance",
    host="172.31.252.109",
    port="3306"
)

# Création d'un curseur pour exécuter les requêtes SQL
cur = conn.cursor()

# Initialisation de Faker
fake = Faker()

# Plage valide d'IDs de compétences
COMPETENCE_ID_MIN = 2056
COMPETENCE_ID_MAX = 2130

# Fonction pour générer un client_id valide
def generate_valid_client_id():
    return random.choice(range(351, 356))  # Choisir un ID valide parmi ceux existants

# Fonction pour générer des missions et les insérer
def generate_mission():
    try:
        # Générer des données aléatoires
        titre = fake.job()
        description = fake.text(max_nb_chars=200)
        budget = round(random.uniform(1000, 50000), 2)
        duree = f"{random.randint(1, 12)} mois"
        statut = random.choice(['EN_ATTENTE', 'ACCEPTEE', 'REFUSEE'])  # Statut valide
        client_id = generate_valid_client_id()  # Utiliser un ID client valide

        # Générer 1 à 3 IDs de compétences dans la plage valide
        competences_ids = random.sample(
            range(COMPETENCE_ID_MIN, COMPETENCE_ID_MAX + 1),
            k=random.randint(1, 3)
        )

        # Insérer la mission dans la table 'missions'
        insert_mission_query = """
            INSERT INTO missions (titre, description, budget, duree, statut, client_id)
            VALUES (%s, %s, %s, %s, %s, %s) RETURNING id;
        """
        cur.execute(insert_mission_query, (titre, description, budget, duree, statut, client_id))

        # Récupérer l'ID de la mission insérée
        mission_id = cur.fetchone()[0]

        # Insérer les compétences dans la table d'association 'mission_competences'
        insert_competence_query = """
            INSERT INTO mission_competences (mission_id, competence_id)
            VALUES (%s, %s);
        """
        for competence_id in competences_ids:
            cur.execute(insert_competence_query, (mission_id, competence_id))

    except Exception as e:
        print(f"Erreur lors de l'insertion de la mission : {e}")
        conn.rollback()  # Annuler la transaction en cas d'erreur

# Générer et insérer 100 missions
for _ in range(100):
    generate_mission()

# Commit des changements dans la base de données
conn.commit()

# Fermer la session et la connexion
cur.close()
conn.close()

print("100 missions insérées avec succès !")
