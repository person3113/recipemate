2025-11-24T02:01:30.689856415Z ==> Cloning from https://github.com/person3113/recipemate
2025-11-24T02:01:32.24819309Z ==> Checking out commit 8290c148c8c08eabf7147c735a61134e8ca77bd5 in branch develop
2025-11-24T02:01:43.990954837Z #1 [internal] load build definition from Dockerfile
2025-11-24T02:01:43.990983488Z #1 transferring dockerfile: 1.35kB done
2025-11-24T02:01:43.990986828Z #1 DONE 0.0s
2025-11-24T02:01:43.990988938Z
2025-11-24T02:01:43.990991688Z #2 [internal] load metadata for docker.io/library/gradle:8.5-jdk21-alpine
2025-11-24T02:01:45.060260414Z #2 ...
2025-11-24T02:01:45.060275315Z
2025-11-24T02:01:45.060278695Z #3 [auth] library/eclipse-temurin:pull render-prod/docker-mirror-repository/library/eclipse-temurin:pull token for us-west1-docker.pkg.dev
2025-11-24T02:01:45.060284775Z #3 DONE 0.0s
2025-11-24T02:01:45.060286945Z
2025-11-24T02:01:45.060289815Z #4 [auth] library/gradle:pull render-prod/docker-mirror-repository/library/gradle:pull token for us-west1-docker.pkg.dev
2025-11-24T02:01:45.060292215Z #4 DONE 0.0s
2025-11-24T02:01:45.210571165Z
2025-11-24T02:01:45.210593266Z #5 [internal] load metadata for docker.io/library/eclipse-temurin:21-jre-alpine
2025-11-24T02:01:49.036546236Z #5 DONE 5.2s
2025-11-24T02:01:49.036566467Z
2025-11-24T02:01:49.036570696Z #2 [internal] load metadata for docker.io/library/gradle:8.5-jdk21-alpine
2025-11-24T02:01:49.459395288Z #2 DONE 5.6s
2025-11-24T02:01:49.644176143Z
2025-11-24T02:01:49.712968469Z #6 [internal] load .dockerignore
2025-11-24T02:01:49.712983459Z #6 transferring context: 735B done
2025-11-24T02:01:49.712987309Z #6 DONE 0.0s
2025-11-24T02:01:49.712990189Z
2025-11-24T02:01:49.712994619Z #7 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T02:01:49.712998159Z #7 resolve docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11 0.0s done
2025-11-24T02:01:50.753129488Z #7 DONE 1.3s
2025-11-24T02:01:50.753146838Z
2025-11-24T02:01:50.753151038Z #8 [internal] load build context
2025-11-24T02:01:50.753154758Z #8 transferring context: 1.75MB 0.1s done
2025-11-24T02:01:50.868588545Z #8 DONE 1.3s
2025-11-24T02:01:50.868610506Z
2025-11-24T02:01:50.868615046Z #9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T02:01:50.868618286Z #9 resolve docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87 0.0s done
2025-11-24T02:01:50.868620826Z #9 DONE 1.4s
2025-11-24T02:01:50.868623366Z
2025-11-24T02:01:50.868625806Z #7 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T02:01:50.973625765Z #7 sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 0B / 2.28kB 0.2s
2025-11-24T02:01:50.973658076Z #7 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 0B / 53.17MB 0.2s
2025-11-24T02:01:50.973661526Z #7 sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 0B / 128B 0.2s
2025-11-24T02:01:51.088800936Z #7 sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 2.28kB / 2.28kB 0.2s done
2025-11-24T02:01:51.088819816Z #7 sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 0B / 16.29MB 0.2s
2025-11-24T02:01:51.238629885Z #7 sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 0B / 3.80MB 0.2s
2025-11-24T02:01:52.288615647Z #7 sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 128B / 128B 1.4s
2025-11-24T02:01:52.588661804Z #7 sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 128B / 128B 1.5s done
2025-11-24T02:01:52.738227957Z #7 sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 3.80MB / 3.80MB 1.7s
2025-11-24T02:01:52.888381354Z #7 sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 16.29MB / 16.29MB 2.0s
2025-11-24T02:01:53.338054785Z #7 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 35.02MB / 53.17MB 2.6s
2025-11-24T02:01:53.338069775Z #7 sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 16.29MB / 16.29MB 2.4s done
2025-11-24T02:01:53.338072535Z #7 sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 3.80MB / 3.80MB 2.0s done
2025-11-24T02:01:53.4885877Z #7 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 53.17MB / 53.17MB 2.7s
2025-11-24T02:01:53.937946764Z #7 extracting sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b
2025-11-24T02:01:55.211238633Z #7 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 53.17MB / 53.17MB 4.1s done
2025-11-24T02:01:55.862823658Z #7 extracting sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 2.0s done
2025-11-24T02:01:55.91434305Z #7 DONE 6.4s
2025-11-24T02:01:55.914372151Z
2025-11-24T02:01:55.914377131Z #9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T02:01:55.914380991Z #9 sha256:1b2010acb2c2f72b7c96a939846c10d9796df96c67f60b271c88f62010e4b002 167B / 167B 1.9s done
2025-11-24T02:01:55.914384431Z #9 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 67.11MB / 132.54MB 2.6s
2025-11-24T02:01:55.914387191Z #9 sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 34.82MB / 34.82MB 2.6s
2025-11-24T02:01:55.914389951Z #9 sha256:516905b70d0910e39f224893c8efc6801b1e1d053bf0031afdb3ef18728a4088 0B / 1.33kB 1.1s
2025-11-24T02:01:55.914392811Z #9 sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 0B / 716B 0.8s
2025-11-24T02:01:56.017160989Z #9 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 90.18MB / 132.54MB 2.7s
2025-11-24T02:01:56.017178789Z #9 sha256:516905b70d0910e39f224893c8efc6801b1e1d053bf0031afdb3ef18728a4088 1.33kB / 1.33kB 1.3s done
2025-11-24T02:01:56.17193688Z #9 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 132.54MB / 132.54MB 2.9s
2025-11-24T02:01:56.171958811Z #9 sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 0B / 179B 0.2s
2025-11-24T02:01:56.367788906Z #9 sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 716B / 716B 1.4s
2025-11-24T02:01:56.508302633Z #9 sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 179B / 179B 0.3s
2025-11-24T02:01:56.768163345Z #9 sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 716B / 716B 1.6s done
2025-11-24T02:01:56.922468475Z #9 sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 34.82MB / 34.82MB 3.5s done
2025-11-24T02:01:57.039485567Z #9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 0B / 158.61MB 0.2s
2025-11-24T02:01:57.16005922Z #9 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 0B / 3.41MB 0.2s
2025-11-24T02:01:57.191063346Z #9 sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 179B / 179B 0.9s done
2025-11-24T02:01:57.191081676Z #9 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 0B / 13.14MB 0.2s
2025-11-24T02:01:58.154923023Z #9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 20.05MB / 158.61MB 1.4s
2025-11-24T02:01:58.545795054Z #9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 38.80MB / 158.61MB 1.7s
2025-11-24T02:01:58.545811915Z #9 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 3.41MB / 3.41MB 1.7s
2025-11-24T02:01:58.647860416Z #9 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 132.54MB / 132.54MB 5.4s done
2025-11-24T02:01:58.648155653Z #9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 52.45MB / 158.61MB 1.8s
2025-11-24T02:01:58.840769225Z #9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 96.47MB / 158.61MB 2.0s
2025-11-24T02:01:58.840786415Z #9 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 3.41MB / 3.41MB 1.8s done
2025-11-24T02:01:58.840790155Z #9 extracting sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8
2025-11-24T02:01:58.99090477Z #9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 130.11MB / 158.61MB 2.1s
2025-11-24T02:01:58.990924821Z #9 extracting sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 0.3s done
2025-11-24T02:01:59.141300811Z #9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 158.61MB / 158.61MB 2.3s
2025-11-24T02:01:59.441256825Z #9 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 13.14MB / 13.14MB 2.4s
2025-11-24T02:02:00.040691571Z #9 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 13.14MB / 13.14MB 2.9s done
2025-11-24T02:02:00.609472799Z #9 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 158.61MB / 158.61MB 3.6s done
2025-11-24T02:02:00.609494149Z #9 extracting sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b
2025-11-24T02:02:01.036431811Z #9 ...
2025-11-24T02:02:01.036446521Z
2025-11-24T02:02:01.036451101Z #7 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T02:02:01.036468291Z #7 extracting sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 5.2s done
2025-11-24T02:02:01.036473182Z #7 DONE 11.6s
2025-11-24T02:02:01.197431503Z
2025-11-24T02:02:01.197456513Z #7 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T02:02:01.197461904Z #7 extracting sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a
2025-11-24T02:02:22.325248073Z #7 ...
2025-11-24T02:02:22.325265623Z
2025-11-24T02:02:22.325269883Z #9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T02:02:22.325273343Z #9 extracting sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 21.9s done
2025-11-24T02:02:22.325276313Z #9 DONE 32.8s
2025-11-24T02:02:22.558475714Z
2025-11-24T02:02:22.558498534Z #7 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T02:02:22.558502435Z #7 extracting sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 21.3s done
2025-11-24T02:02:22.558506055Z #7 extracting sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 0.1s done
2025-11-24T02:02:22.558510975Z #7 extracting sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 0.1s done
2025-11-24T02:02:22.558515945Z #7 DONE 33.1s
2025-11-24T02:02:22.558520295Z
2025-11-24T02:02:22.558525025Z #9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T02:02:22.558529415Z #9 extracting sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e
2025-11-24T02:02:24.211756422Z #9 ...
2025-11-24T02:02:24.211772842Z
2025-11-24T02:02:24.211784652Z #10 [stage-1 2/5] WORKDIR /app
2025-11-24T02:02:24.211786742Z #10 DONE 1.7s
2025-11-24T02:02:24.386178116Z
2025-11-24T02:02:24.393981563Z #11 [stage-1 3/5] RUN addgroup -S spring && adduser -S spring -G spring
2025-11-24T02:02:24.970506057Z #11 ...
2025-11-24T02:02:24.970526258Z
2025-11-24T02:02:24.970532558Z #9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T02:02:24.970538278Z #9 extracting sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 2.6s done
2025-11-24T02:02:24.970542898Z #9 DONE 35.5s
2025-11-24T02:02:25.102097448Z
2025-11-24T02:02:25.102121408Z #11 [stage-1 3/5] RUN addgroup -S spring && adduser -S spring -G spring
2025-11-24T02:02:25.102127209Z #11 DONE 0.8s
2025-11-24T02:02:25.102131169Z
2025-11-24T02:02:25.102134829Z #9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T02:02:25.102138229Z #9 extracting sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 0.0s done
2025-11-24T02:02:25.102141059Z #9 extracting sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 0.0s done
2025-11-24T02:02:25.102143799Z #9 extracting sha256:516905b70d0910e39f224893c8efc6801b1e1d053bf0031afdb3ef18728a4088 0.1s done
2025-11-24T02:02:25.102146599Z #9 DONE 35.6s
2025-11-24T02:02:25.102149259Z
2025-11-24T02:02:25.102152089Z #12 [stage-1 4/5] RUN mkdir -p /app/uploads && chown -R spring:spring /app
2025-11-24T02:02:25.202426878Z #12 DONE 0.1s
2025-11-24T02:02:25.202444838Z
2025-11-24T02:02:25.202451129Z #9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T02:02:25.202455379Z #9 extracting sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd
2025-11-24T02:02:26.311220908Z #9 extracting sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 1.2s done
2025-11-24T02:02:26.311241919Z #9 DONE 36.8s
2025-11-24T02:02:26.461703308Z
2025-11-24T02:02:26.461751369Z #9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T02:02:26.461758139Z #9 extracting sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96
2025-11-24T02:02:27.847277467Z #9 extracting sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 1.5s done
2025-11-24T02:02:27.847292968Z #9 DONE 38.4s
2025-11-24T02:02:28.010166089Z
2025-11-24T02:02:28.01018764Z #9 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T02:02:28.01019243Z #9 extracting sha256:1b2010acb2c2f72b7c96a939846c10d9796df96c67f60b271c88f62010e4b002 0.1s done
2025-11-24T02:02:28.01019539Z #9 DONE 38.4s
2025-11-24T02:02:28.01019809Z
2025-11-24T02:02:28.0102016Z #13 [builder 2/7] WORKDIR /app
2025-11-24T02:02:28.01020448Z #13 DONE 0.0s
2025-11-24T02:02:28.01020896Z
2025-11-24T02:02:28.01021472Z #14 [builder 3/7] COPY build.gradle settings.gradle gradlew ./
2025-11-24T02:02:28.01021942Z #14 DONE 0.1s
2025-11-24T02:02:28.216251343Z
2025-11-24T02:02:28.216279923Z #15 [builder 4/7] COPY gradle gradle/
2025-11-24T02:02:28.216284753Z #15 DONE 0.1s
2025-11-24T02:02:28.216288553Z
2025-11-24T02:02:28.216292893Z #16 [builder 5/7] RUN gradle dependencies --no-daemon || true
2025-11-24T02:02:28.772107105Z #16 0.706
2025-11-24T02:02:28.87620593Z #16 0.706 Welcome to Gradle 8.5!
2025-11-24T02:02:28.876238071Z #16 0.706
2025-11-24T02:02:28.876241751Z #16 0.707 Here are the highlights of this release:
2025-11-24T02:02:28.876244871Z #16 0.707  - Support for running on Java 21
2025-11-24T02:02:28.876247151Z #16 0.707  - Faster first use with Kotlin DSL
2025-11-24T02:02:28.876249521Z #16 0.707  - Improved error and warning messages
2025-11-24T02:02:28.876251711Z #16 0.707
2025-11-24T02:02:28.876254531Z #16 0.707 For more details see https://docs.gradle.org/8.5/release-notes.html
2025-11-24T02:02:28.876261672Z #16 0.707
2025-11-24T02:02:28.876266532Z #16 0.809 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
2025-11-24T02:02:30.371121282Z #16 2.305 Daemon will be stopped at the end of the build
2025-11-24T02:02:53.971204825Z #16 25.91
2025-11-24T02:02:54.122941781Z #16 25.91 > Task :dependencies
2025-11-24T02:02:54.122962161Z #16 25.91
2025-11-24T02:02:54.122967551Z #16 25.91
2025-11-24T02:03:08.035301644Z
2025-11-24T02:03:08.035327784Z #17 [builder 6/7] COPY src src/
2025-11-24T02:03:10.542120229Z #17 DONE 2.7s
2025-11-24T02:03:10.695279045Z
2025-11-24T02:03:10.695307545Z #18 [builder 7/7] RUN gradle bootJar --no-daemon -x test
2025-11-24T02:03:11.507290643Z #18 0.963 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
2025-11-24T02:03:13.108532091Z #18 2.564 Daemon will be stopped at the end of the build
2025-11-24T02:03:58.812571718Z #18 48.27 > Task :compileJava
2025-11-24T02:04:19.407420901Z #18 68.86 Note: Some input files use or override a deprecated API.
2025-11-24T02:04:19.697643965Z #18 68.86 Note: Recompile with -Xlint:deprecation for details.
2025-11-24T02:04:19.697805939Z #18 68.86 Note: /app/src/main/java/com/recipemate/global/util/ImageUploadUtil.java uses unchecked or unsafe operations.
2025-11-24T02:04:19.697820539Z #18 68.96 Note: Recompile with -Xlint:unchecked for details.
2025-11-24T02:04:35.306542969Z #18 84.76
2025-11-24T02:04:35.497470242Z #18 84.76 > Task :processResources
2025-11-24T02:04:35.497492043Z #18 84.76 > Task :classes
2025-11-24T02:04:35.608332392Z #18 85.06 > Task :resolveMainClassName
2025-11-24T02:04:43.498933934Z #18 92.95 > Task :bootJar
2025-11-24T02:04:43.649747348Z #18 92.95
2025-11-24T02:04:43.649765608Z #18 92.95 BUILD SUCCESSFUL in 1m 32s
2025-11-24T02:04:43.649768968Z #18 92.95 4 actionable tasks: 4 executed
2025-11-24T02:04:43.835057022Z #18 DONE 93.3s
2025-11-24T02:04:44.196863882Z
2025-11-24T02:04:44.196883723Z #19 [stage-1 5/5] COPY --from=builder /app/build/libs/*.jar app.jar
2025-11-24T02:04:46.117823564Z #19 DONE 1.9s
2025-11-24T02:04:46.273052087Z
2025-11-24T02:04:46.273077998Z #20 exporting to docker image format
2025-11-24T02:04:46.273085428Z #20 exporting layers
2025-11-24T02:04:48.286321416Z #20 exporting layers 2.2s done
2025-11-24T02:04:48.497424123Z #20 exporting manifest sha256:1c1301ec28e2432115829dff1a915ea684948282fd5bd58a4a3a270828a02353 done
2025-11-24T02:04:48.497460084Z #20 exporting config sha256:9186e131e6b3f446f9ed3bde2404d8ff4137984984eb75c1d87966803c8c661d done
2025-11-24T02:04:49.130046532Z #20 DONE 3.0s
2025-11-24T02:04:49.280386075Z
2025-11-24T02:04:49.281446308Z #21 exporting cache to client directory
2025-11-24T02:04:49.281455879Z #21 preparing build cache for export
2025-11-24T02:05:08.601336069Z #21 writing cache image manifest sha256:723d650efc4cb857f927bde08a86c1ba549038097475df3c8777243129e69f87 done
2025-11-24T02:05:08.601355069Z #21 DONE 19.4s
2025-11-24T02:05:13.618322707Z Pushing image to registry...
2025-11-24T02:05:17.981537445Z Upload succeeded
2025-11-24T02:05:23.804944415Z ==> Deploying...
2025-11-24T02:06:29.491495667Z
2025-11-24T02:06:29.491528489Z   .   ____          _            __ _ _
2025-11-24T02:06:29.491533789Z  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
2025-11-24T02:06:29.491536799Z ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
2025-11-24T02:06:29.491539629Z  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
2025-11-24T02:06:29.49154238Z   '  |____| .__|_| |_|_| |_\__, | / / / /
2025-11-24T02:06:29.49154548Z  =========|_|==============|___/=/_/_/_/
2025-11-24T02:06:29.49154781Z
2025-11-24T02:06:29.491894116Z  :: Spring Boot ::                (v3.5.7)
2025-11-24T02:06:29.491900336Z
2025-11-24T02:06:30.99231084Z 2025-11-24T02:06:30.988Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : Starting RecipeMateApplication v0.0.1-SNAPSHOT using Java 21.0.9 with PID 1 (/app/app.jar started by spring in /app)
2025-11-24T02:06:30.99362662Z 2025-11-24T02:06:30.992Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : The following 1 profile is active: "prod"
2025-11-24T02:06:31.352779374Z ==> No open ports detected, continuing to scan...
2025-11-24T02:06:31.780918251Z ==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
2025-11-24T02:06:55.187032508Z 2025-11-24T02:06:55.186Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2025-11-24T02:06:55.189664219Z 2025-11-24T02:06:55.189Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2025-11-24T02:06:59.691887132Z 2025-11-24T02:06:59.690Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 4395 ms. Found 25 JPA repository interfaces.
2025-11-24T02:07:00.287392969Z 2025-11-24T02:07:00.286Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2025-11-24T02:07:00.28914729Z 2025-11-24T02:07:00.288Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2025-11-24T02:07:00.889790473Z 2025-11-24T02:07:00.889Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.badge.repository.BadgeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:00.891934871Z 2025-11-24T02:07:00.891Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.comment.repository.CommentRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:00.892423184Z 2025-11-24T02:07:00.892Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.directmessage.repository.DirectMessageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:00.892633753Z 2025-11-24T02:07:00.892Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:00.892910166Z 2025-11-24T02:07:00.892Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.GroupBuyRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:00.987339326Z 2025-11-24T02:07:00.986Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.ParticipationRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:00.987698903Z 2025-11-24T02:07:00.987Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.like.repository.CommentLikeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:00.988364133Z 2025-11-24T02:07:00.988Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.like.repository.PostLikeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:00.988697579Z 2025-11-24T02:07:00.988Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.notification.repository.NotificationRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.096325864Z 2025-11-24T02:07:00.988Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.post.repository.PostImageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.096366846Z 2025-11-24T02:07:01.090Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.post.repository.PostRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.096370716Z 2025-11-24T02:07:01.091Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeCorrectionRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.097862554Z 2025-11-24T02:07:01.097Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeIngredientRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.098962545Z 2025-11-24T02:07:01.098Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.09950757Z 2025-11-24T02:07:01.099Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeStepRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.100066195Z 2025-11-24T02:07:01.099Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipewishlist.repository.RecipeWishlistRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.102454895Z 2025-11-24T02:07:01.100Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.report.repository.ReportRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.187924004Z 2025-11-24T02:07:01.187Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.review.repository.ReviewRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.189319988Z 2025-11-24T02:07:01.188Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.search.repository.SearchKeywordRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.194340118Z 2025-11-24T02:07:01.193Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.AddressRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.194898274Z 2025-11-24T02:07:01.194Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.MannerTempHistoryRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.19546279Z 2025-11-24T02:07:01.195Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.PersistentTokenJpaRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.195994234Z 2025-11-24T02:07:01.195Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.PointHistoryRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.198270879Z 2025-11-24T02:07:01.196Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.UserRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.198330711Z 2025-11-24T02:07:01.196Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.wishlist.repository.WishlistRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T02:07:01.198367213Z 2025-11-24T02:07:01.196Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 705 ms. Found 0 Redis repository interfaces.
2025-11-24T02:07:18.590434816Z 2025-11-24T02:07:18.590Z  INFO 1 --- [RecipeMate] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2025-11-24T02:07:18.79243974Z 2025-11-24T02:07:18.792Z  INFO 1 --- [RecipeMate] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-11-24T02:07:18.79268044Z 2025-11-24T02:07:18.792Z  INFO 1 --- [RecipeMate] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.48]
2025-11-24T02:07:20.991483327Z 2025-11-24T02:07:20.991Z  INFO 1 --- [RecipeMate] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-11-24T02:07:20.992612679Z 2025-11-24T02:07:20.992Z  INFO 1 --- [RecipeMate] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 48599 ms
2025-11-24T02:07:28.788732392Z 2025-11-24T02:07:28.786Z  INFO 1 --- [RecipeMate] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2025-11-24T02:07:29.99553088Z 2025-11-24T02:07:29.995Z  INFO 1 --- [RecipeMate] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.33.Final
2025-11-24T02:07:30.693342788Z 2025-11-24T02:07:30.693Z  INFO 1 --- [RecipeMate] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2025-11-24T02:07:34.901170224Z ==> No open ports detected, continuing to scan...
2025-11-24T02:07:35.40778094Z ==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
2025-11-24T02:07:36.589203563Z 2025-11-24T02:07:36.588Z  INFO 1 --- [RecipeMate] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2025-11-24T02:07:37.395057526Z 2025-11-24T02:07:37.393Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-11-24T02:07:43.799731951Z 2025-11-24T02:07:43.799Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@779448b8
2025-11-24T02:07:43.800952377Z 2025-11-24T02:07:43.800Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-11-24T02:07:44.791217916Z 2025-11-24T02:07:44.790Z  WARN 1 --- [RecipeMate] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2025-11-24T02:07:45.592270148Z 2025-11-24T02:07:45.590Z  INFO 1 --- [RecipeMate] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
2025-11-24T02:07:45.592295769Z 	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
2025-11-24T02:07:45.592299709Z 	Database driver: undefined/unknown
2025-11-24T02:07:45.592302629Z 	Database version: 18.1
2025-11-24T02:07:45.592305539Z 	Autocommit mode: undefined/unknown
2025-11-24T02:07:45.59231073Z 	Isolation level: undefined/unknown
2025-11-24T02:07:45.592313419Z 	Minimum pool size: undefined/unknown
2025-11-24T02:07:45.59231604Z 	Maximum pool size: undefined/unknown
2025-11-24T02:07:51.491212272Z 2025-11-24T02:07:51.490Z  WARN 1 --- [RecipeMate] [           main] o.h.boot.model.internal.ToOneBinder      : HHH000491: 'com.recipemate.domain.recipewishlist.entity.RecipeWishlist.recipe' uses both @NotFound and FetchType.LAZY. @ManyToOne and @OneToOne associations mapped with @NotFound are forced to EAGER fetching.
2025-11-24T02:08:14.989988462Z 2025-11-24T02:08:14.989Z  INFO 1 --- [RecipeMate] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2025-11-24T02:08:16.389779738Z 2025-11-24T02:08:16.389Z  INFO 1 --- [RecipeMate] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2025-11-24T02:08:23.289964179Z 2025-11-24T02:08:23.289Z  INFO 1 --- [RecipeMate] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2025-11-24T02:08:38.772512533Z ==> No open ports detected, continuing to scan...
2025-11-24T02:08:39.224418971Z ==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
2025-11-24T02:09:05.791047651Z ==> Out of memory (used over 512Mi)
2025-11-24T02:09:05.96926406Z ==> Common ways to troubleshoot your deploy: https://render.com/docs/troubleshooting-deploys