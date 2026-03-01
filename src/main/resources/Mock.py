import mysql.connector
from faker import Faker
import random

fake = Faker("fr_FR")

DB_HOST = "172.31.252.109",
DB_PORT = 3306
DB_NAME = "freelancing_db",
DB_USER = "freelancing_user",
DB_PASSWORD = "root",

NB_CLIENTS = 5000
NB_TECH_EXPERTS = 6000
NB_SERVICES = 10000

EMAIL_CACHE = set()

def get_unique_email():
    while True:
        email = fake.email()
        if email not in EMAIL_CACHE:
            EMAIL_CACHE.add(email)
            return email



    cur.execute(
        """
        INSERT INTO address (street, city, pincode)
        VALUES (%s, %s, %s)
        """,
        (street, city, pincode),
    )
    return cur.lastrowid

def create_user(cur, role: str):
    first_name = fake.first_name()
    last_name = fake.last_name()
    shop_name = fake.company() if role == "TECH_EXPERT" else None
    email_id = get_unique_email()
    password = "pwd"  # TODO: hash
    phone_no = fake.phone_number()
    wallet_amount = round(random.uniform(0, 1000), 2)
    status = random.choice(["ACTIVE", "INACTIVE"])

    address_id = create_address(cur)

    if shop_name:
        cur.execute(
            """
            INSERT INTO `user`
                (first_name, last_name, shop_name, email_id, password,
                 phone_no, role, address_id, wallet_amount, status)
            VALUES (%s, %s, %s, %s, %s,
                    %s, %s, %s, %s, %s)
            """,
            (
                first_name, last_name, shop_name, email_id, password,
                phone_no, role, address_id, wallet_amount, status,
            ),
        )
    else:
        cur.execute(
            """
            INSERT INTO `user`
                (first_name, last_name, email_id, password,
                 phone_no, role, address_id, wallet_amount, status)
            VALUES (%s, %s, %s, %s,
                    %s, %s, %s, %s)
            """,
            (
                first_name, last_name, email_id, password,
                phone_no, role, address_id, wallet_amount, status,
            ),
        )

    return cur.lastrowid

def create_users(cur):
    client_ids = []
    tech_expert_ids = []

    for _ in range(NB_CLIENTS):
        client_ids.append(create_user(cur, "CLIENT"))

    for _ in range(NB_TECH_EXPERTS):
        tech_expert_ids.append(create_user(cur, "TECH_EXPERT"))

    return client_ids, tech_expert_ids

def create_service(cur, tech_expert_id, category_id=None):
    name = fake.job()
    description = fake.text(max_nb_chars=400)
    added_time = fake.iso8601()
    min_price = round(random.uniform(50, 2000), 2)
    delivery_time = random.randint(1, 30)
    image1 = "https://picsum.photos/seed/" + str(random.randint(1, 10000)) + "/600/400"
    image2 = None
    image3 = None
    status = random.choice(["ACTIVE", "INACTIVE"])

    if category_id:
        cur.execute(
            """
            INSERT INTO service
                (name, description, category_id, tech_expert_id,
                 added_time, min_price, delivery_time,
                 image1, image2, image3, status)
            VALUES (%s, %s, %s, %s,
                    %s, %s, %s,
                    %s, %s, %s, %s)
            """,
            (
                name, description, category_id, tech_expert_id,
                added_time, min_price, delivery_time,
                image1, image2, image3, status,
            ),
        )
    else:
        cur.execute(
            """
            INSERT INTO service
                (name, description, tech_expert_id,
                 added_time, min_price, delivery_time,
                 image1, status)
            VALUES (%s, %s, %s,
                    %s, %s, %s,
                    %s, %s)
            """,
            (
                name, description, tech_expert_id,
                added_time, min_price, delivery_time,
                image1, status,
            ),
        )

def create_services(cur, tech_expert_ids):
    for _ in range(NB_SERVICES):
        tech_expert_id = random.choice(tech_expert_ids)
        create_service(cur, tech_expert_id)

def main():
    conn = mysql.connector.connect(
    DB_HOST = "172.31.252.109",
    DB_PORT = "3306",
    DB_NAME = "freelancing_db",
    DB_USER = "root",
    DB_PASSWORD = "root",
    )
    cur = conn.cursor()
    try:
        client_ids, tech_expert_ids = create_users(cur)
        create_services(cur, tech_expert_ids)
        conn.commit()
        print("Users + services insérés avec succès !")
    except Exception as e:
        conn.rollback()
        print("Erreur, rollback :", e)
    finally:
        cur.close()
        conn.close()

if __name__ == "__main__":
    main()
