CREATE TABLE IF NOT EXISTS SHOP    (
    NAME VARCHAR(100);
    LATITUDE DOUBLE;
    LONGITUDE DOUBLE;
    ADDRESS VARCHAR (100)
    URL VARCHAR (100);
    ACTIVE BOOLEAN;
    PRIMARY KEY (NAME, LATITUDE, LONGITUDE)
);

CREATE TABLE IF NOT EXISTS OPENING_HOURS(
     SHOP_NAME VARCHAR(100);
     SHOP_LATITUDE DOUBLE;
     SHOP_LONGITUDE DOUBLE;
     DAY VARCHAR(10);
     HOURS VARCHAR(100);
     FOREIGN KEY(SHOP_NAME, SHOP_LATITUDE, SHOP_LONGITUDE) REFERENCES SHOPS(NAME, LATITUDE, LONGITUDE)
);