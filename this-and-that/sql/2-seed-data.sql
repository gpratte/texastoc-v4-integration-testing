-- noinspection SqlNoDataSourceInspectionForFile
-- Run this file from command line
--   mysql -u <user> -p < 2-seed-data.sql

INSERT INTO season_payout_settings
VALUES (1, 2021, '[
  {
    "lowRange":16000,
    "highRange":20000,
    "guaranteed":[
      {
        "place":1,
        "amount":4400,
        "percent":20
      }
    ],
    "finalTable":[
      {
        "place":2,
        "amount":4000,
        "percent":20
      },
      {
        "place":3,
        "amount":3850,
        "percent":16
      },
      {
        "place":4,
        "amount":3750,
        "percent":14
      },
      {
        "place":5,
        "amount":0,
        "percent":30
      }
    ]
  },
  {
    "lowRange":20000,
    "highRange":24000,
    "guaranteed":[
      {
        "place":1,
        "amount":5000,
        "percent":15
      },
      {
        "place":2,
        "amount":4500,
        "percent":8
      }
    ],
    "finalTable":[
      {
        "place":3,
        "amount":4000,
        "percent":12
      },
      {
        "place":4,
        "amount":3500,
        "percent":9
      },
      {
        "place":5,
        "amount":3000,
        "percent":6
      },
      {
        "place":6,
        "amount":0,
        "percent":50
      }
    ]
  },
  {
    "lowRange":24000,
    "highRange":28000,
    "guaranteed":[
      {
        "place":1,
        "amount":5500,
        "percent":15
      },
      {
        "place":2,
        "amount":4800,
        "percent":5
      }
    ],
    "finalTable":[
      {
        "place":3,
        "amount":4000,
        "percent":14
      },
      {
        "place":4,
        "amount":3700,
        "percent":10
      },
      {
        "place":5,
        "amount":3000,
        "percent":6
      },
      {
        "place":6,
        "amount":3000,
        "percent":0
      },
      {
        "place":7,
        "amount":0,
        "percent":50
      }
    ]
  }
]');
INSERT INTO version
VALUES (1, '3.00');
INSERT INTO settings
VALUES (1, 1);
INSERT INTO toc_config
VALUES (1, 0, 20, 20, 3, 40, 40, 20, 2021, 1);
