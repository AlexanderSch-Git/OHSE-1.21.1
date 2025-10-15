-- ==========================================================
-- OHSE Zones, Mobs, Biomes, Tags Schema
-- ==========================================================

-- --- Zones table ------------------------------------
CREATE TABLE IF NOT EXISTS zones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid VARCHAR(36) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  creator VARCHAR(64) NOT NULL,
  world VARCHAR(64) NOT NULL,
  yMin DOUBLE NOT NULL,
  yMax DOUBLE NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --- Zone chunks table ------------------------------------
CREATE TABLE IF NOT EXISTS zone_chunks (
  id INT AUTO_INCREMENT PRIMARY KEY,
  zone_uuid VARCHAR(36) NOT NULL,
  world VARCHAR(64) NOT NULL,
  chunk_x INT NOT NULL,
  chunk_z INT NOT NULL,
  UNIQUE KEY uq_zone_chunk (zone_uuid, chunk_x, chunk_z),
  INDEX idx_chunk_lookup (chunk_x, chunk_z),
  CONSTRAINT fk_zone_chunks_zone FOREIGN KEY (zone_uuid)
      REFERENCES zones(uuid)
      ON DELETE CASCADE
) ENGINE=InnoDB;

-- --- Mobs table ------------------------------------
CREATE TABLE IF NOT EXISTS mobs (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid VARCHAR(36) NOT NULL UNIQUE,
  mod_id VARCHAR(128) NOT NULL,         -- ex: minecraft:zombie
  display_name VARCHAR(64) NOT NULL,

  -- Spawn timing
  start_tick INT NOT NULL DEFAULT 0,
  end_tick INT NOT NULL DEFAULT 24000,

  -- Environmental conditions
  can_spawn_in_rain BOOLEAN NOT NULL DEFAULT TRUE,
  needs_rain BOOLEAN NOT NULL DEFAULT FALSE,
  can_spawn_in_water BOOLEAN NOT NULL DEFAULT FALSE,
  needs_water BOOLEAN NOT NULL DEFAULT FALSE,
  can_spawn_in_air BOOLEAN NOT NULL DEFAULT FALSE,
  needs_solid_block BOOLEAN NOT NULL DEFAULT TRUE,

  -- Spawn balance
  spawn_weight INT NOT NULL DEFAULT 10,
  spawn_cap INT NOT NULL DEFAULT 64,                     -- 0 = no global limit

  -- Spawn space requirements
  squared_space_required_above_horizontal INT NOT NULL DEFAULT 3,  -- in blocks mean like a 3x3 area around the center
  space_required_below INT NOT NULL DEFAULT 2,           -- in blocks means can't spawn over a floating block
  space_required_above INT NOT NULL DEFAULT 2,           -- in blocks means can't spawn under a low ceiling
  -- Group behavior
  group_min INT NOT NULL DEFAULT 1,
  group_max INT NOT NULL DEFAULT 5,
  group_distance INT NOT NULL DEFAULT 8,                -- radius in blocks
  can_be_group_leader BOOLEAN NOT NULL DEFAULT FALSE,
  max_group_leaders_per_horde INT NULL,
  requires_leader BOOLEAN NOT NULL DEFAULT FALSE,
  required_leader_mob_id INT NULL,
  required_leader_count INT NULL,

  tags TEXT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_required_leader
        FOREIGN KEY (required_leader_mob_id) REFERENCES mobs(id) ON DELETE RESTRICT,

    -- 🔒 Unicity on mod_id
    UNIQUE KEY uq_mobs_modid (mod_id)
) ENGINE=InnoDB;

-- --- Biomes table ----------------------------------
CREATE TABLE IF NOT EXISTS biomes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  mod_id VARCHAR(128) NOT NULL UNIQUE,  -- ex: minecraft:plains
  display_name VARCHAR(64) NOT NULL,
  category VARCHAR(64) NULL,
  temperature FLOAT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --- Mob ↔ Biome relation --------------------------
CREATE TABLE IF NOT EXISTS mob_biomes (
  mob_id INT NOT NULL,
  biome_id INT NOT NULL,
  PRIMARY KEY (mob_id, biome_id),
  FOREIGN KEY (mob_id) REFERENCES mobs(id) ON DELETE CASCADE,
  FOREIGN KEY (biome_id) REFERENCES biomes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --- Tags table ------------------------------------
CREATE TABLE IF NOT EXISTS tags (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) NOT NULL UNIQUE,
  description VARCHAR(255) NULL
) ENGINE=InnoDB;

-- --- Mob ↔ Tag relation ----------------------------
CREATE TABLE IF NOT EXISTS mob_tags (
  mob_id INT NOT NULL,
  tag_id INT NOT NULL,
  PRIMARY KEY (mob_id, tag_id),
  FOREIGN KEY (mob_id) REFERENCES mobs(id) ON DELETE CASCADE,
  FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --- Optional: Flexible leader mapping --------------
-- Use this only if you want multiple possible leaders per follower type
CREATE TABLE IF NOT EXISTS mob_leader_requirements (
  follower_mob_id INT NOT NULL,
  leader_mob_id INT NOT NULL,
  leader_count INT NOT NULL DEFAULT 1,
  PRIMARY KEY (follower_mob_id, leader_mob_id),
  FOREIGN KEY (follower_mob_id) REFERENCES mobs(id) ON DELETE CASCADE,
  FOREIGN KEY (leader_mob_id) REFERENCES mobs(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

