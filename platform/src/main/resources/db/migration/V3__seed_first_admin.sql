INSERT INTO users (
    email,
    mot_de_passe_hash,
    first_name,
    last_name,
    phone_number,
    role,
    active,
    date_creation,
    updated_at
) VALUES (
             'skabore2020@gmail.com',
             '$2b$10$rMBU.ihLixA1yawu0VRq/uGyiveYZ0Ib/q4EfTo7sszh52lGSy82i',
             'Seydou',
             'Kaboré',
             NULL,
             'ADMIN',
             true,
             now(),
             now()
         );