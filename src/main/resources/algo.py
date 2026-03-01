import psycopg2
import random

# Connexion à la base de données MySQL
conn = psycopg2.connect(
    dbname="freelancing_db",
    user="root",
    password="root",
    host="172.31.252.109",
    port="3306"
)

cur = conn.cursor()

# Plages d'ID
mission_ids = list(range(25, 873))
freelancer_ids = list(range(21, 202)) + list(range(2859, 2889)) + list(range(2759, 2859))


# Associer aléatoirement des freelancers à des missions
def associate_missions_with_freelancers():
    try:
        for mission_id in mission_ids:
            # Sélectionner un freelancer aléatoire
            freelancer_id = random.choice(freelancer_ids)

            # Insérer dans la table Mission_Freelance
            insert_query = """
                INSERT INTO "Mission_Freelance" (mission_id, freelancer_id)
                VALUES (%s, %s);
            """
            cur.execute(insert_query, (mission_id, freelancer_id))
    except Exception as e:
        print(f"Erreur lors de l'insertion dans Mission_Freelance : {e}")
        conn.rollback()  # Annuler la transaction en cas d'erreur
    else:
        conn.commit()  # Valider les changements si tout est OK
        print("Associations insérées avec succès !")


# Exécuter l'association
associate_missions_with_freelancers()

# Fermer les connexions
cur.close()
conn.close()
