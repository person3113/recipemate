2025-11-24T00:55:27.179816222Z ==> Cloning from https://github.com/person3113/recipemate
2025-11-24T00:55:28.623419245Z ==> Checking out commit 8a17e40085635cf8e79c1081bd67ab624a9f91c0 in branch develop
2025-11-24T00:55:38.389979376Z #1 [internal] load build definition from Dockerfile
2025-11-24T00:55:38.563519004Z #1 transferring dockerfile: 1.35kB done
2025-11-24T00:55:38.563535174Z #1 DONE 0.0s
2025-11-24T00:55:38.563538634Z
2025-11-24T00:55:38.563544254Z #2 [internal] load metadata for docker.io/library/gradle:8.5-jdk21-alpine
2025-11-24T00:55:39.659810565Z #2 ...
2025-11-24T00:55:39.659826345Z
2025-11-24T00:55:39.659829845Z #3 [auth] library/gradle:pull render-prod/docker-mirror-repository/library/gradle:pull token for us-west1-docker.pkg.dev
2025-11-24T00:55:39.659836125Z #3 DONE 0.0s
2025-11-24T00:55:39.659838215Z
2025-11-24T00:55:39.659840965Z #4 [auth] library/eclipse-temurin:pull render-prod/docker-mirror-repository/library/eclipse-temurin:pull token for us-west1-docker.pkg.dev
2025-11-24T00:55:39.659843755Z #4 DONE 0.0s
2025-11-24T00:55:39.809061594Z
2025-11-24T00:55:39.809085825Z #5 [internal] load metadata for docker.io/library/eclipse-temurin:21-jre-alpine
2025-11-24T00:55:43.585182702Z #5 DONE 5.2s
2025-11-24T00:55:43.585197592Z
2025-11-24T00:55:43.585201112Z #2 [internal] load metadata for docker.io/library/gradle:8.5-jdk21-alpine
2025-11-24T00:55:44.223233267Z #2 DONE 5.8s
2025-11-24T00:55:44.374371884Z
2025-11-24T00:55:44.40875488Z #6 [internal] load .dockerignore
2025-11-24T00:55:44.874681672Z #6 transferring context:
2025-11-24T00:55:45.027063822Z #6 transferring context: 735B done
2025-11-24T00:55:45.076656515Z #6 DONE 0.9s
2025-11-24T00:55:45.250959052Z
2025-11-24T00:55:45.250988683Z #7 [internal] load build context
2025-11-24T00:55:45.250995923Z #7 DONE 0.0s
2025-11-24T00:55:45.251001483Z
2025-11-24T00:55:45.251006973Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:55:45.251012763Z #8 resolve docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:55:46.45826827Z #8 resolve docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87 1.2s done
2025-11-24T00:55:50.481244002Z #8 ...
2025-11-24T00:55:50.481262463Z
2025-11-24T00:55:50.481267643Z #7 [internal] load build context
2025-11-24T00:55:50.481272553Z #7 transferring context: 1.75MB 0.1s done
2025-11-24T00:55:50.481277013Z #7 DONE 4.0s
2025-11-24T00:55:50.481280943Z
2025-11-24T00:55:50.481285903Z #9 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T00:55:50.481290663Z #9 resolve docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11 1.4s done
2025-11-24T00:55:57.187044298Z #9 DONE 12.0s
2025-11-24T00:55:57.187063018Z
2025-11-24T00:55:57.187067868Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:55:57.504533721Z #8 ...
2025-11-24T00:55:57.504556391Z
2025-11-24T00:55:57.504562892Z #9 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T00:55:58.047285329Z #9 sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 0B / 2.28kB 0.2s
2025-11-24T00:55:58.442822699Z #9 sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 0B / 128B 0.2s
2025-11-24T00:55:58.569171846Z #9 sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 0B / 16.29MB 0.2s
2025-11-24T00:55:59.018834404Z #9 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 0B / 53.17MB 0.2s
2025-11-24T00:55:59.169713195Z #9 sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 2.28kB / 2.28kB 1.2s done
2025-11-24T00:55:59.319445347Z #9 sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 0B / 3.80MB 0.2s
2025-11-24T00:55:59.441799842Z #9 ...
2025-11-24T00:55:59.441820433Z
2025-11-24T00:55:59.441827083Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:55:59.441833023Z #8 DONE 14.3s
2025-11-24T00:55:59.441837983Z
2025-11-24T00:55:59.441843793Z #9 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T00:55:59.441848984Z #9 sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 128B / 128B 1.1s done
2025-11-24T00:56:00.178655272Z #9 sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 3.80MB / 3.80MB 1.1s done
2025-11-24T00:56:00.278947166Z #9 extracting sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 0.1s done
2025-11-24T00:56:01.36045932Z #9 sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 8.39MB / 16.29MB 2.9s
2025-11-24T00:56:01.360478401Z #9 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 5.24MB / 53.17MB 2.6s
2025-11-24T00:56:01.510191422Z #9 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 8.39MB / 53.17MB 2.7s
2025-11-24T00:56:01.809529453Z #9 sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 16.29MB / 16.29MB 3.3s done
2025-11-24T00:56:01.809547804Z #9 extracting sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4
2025-11-24T00:56:02.110109756Z #9 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 19.92MB / 53.17MB 3.3s
2025-11-24T00:56:02.260403092Z #9 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 33.55MB / 53.17MB 3.5s
2025-11-24T00:56:02.559725313Z #9 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 41.94MB / 53.17MB 3.8s
2025-11-24T00:56:03.698234719Z #9 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 53.17MB / 53.17MB 5.0s
2025-11-24T00:56:05.33028331Z #9 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 53.17MB / 53.17MB 6.3s done
2025-11-24T00:56:06.981017747Z #9 ...
2025-11-24T00:56:06.981037137Z
2025-11-24T00:56:06.981041907Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:56:06.981046627Z #8 sha256:1b2010acb2c2f72b7c96a939846c10d9796df96c67f60b271c88f62010e4b002 167B / 167B 1.1s done
2025-11-24T00:56:06.981050777Z #8 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 67.11MB / 132.54MB 6.8s
2025-11-24T00:56:06.981054417Z #8 sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 34.82MB / 34.82MB 6.3s
2025-11-24T00:56:06.981057877Z #8 sha256:516905b70d0910e39f224893c8efc6801b1e1d053bf0031afdb3ef18728a4088 1.33kB / 1.33kB 1.4s done
2025-11-24T00:56:06.981061328Z #8 sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 716B / 716B 0.7s done
2025-11-24T00:56:06.981064878Z #8 sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 179B / 179B 0.4s done
2025-11-24T00:56:06.981068308Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 0B / 158.61MB 2.1s
2025-11-24T00:56:06.981071758Z #8 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 0B / 13.14MB 1.7s
2025-11-24T00:56:07.13867971Z #8 sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 34.82MB / 34.82MB 6.3s done
2025-11-24T00:56:07.281683201Z #8 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 92.27MB / 132.54MB 7.1s
2025-11-24T00:56:07.281700132Z #8 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 0B / 3.41MB 0.2s
2025-11-24T00:56:07.881334058Z #8 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 100.66MB / 132.54MB 7.7s
2025-11-24T00:56:08.18072817Z #8 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 117.44MB / 132.54MB 8.0s
2025-11-24T00:56:08.330828381Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 8.39MB / 158.61MB 3.5s
2025-11-24T00:56:08.480885782Z #8 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 8.39MB / 13.14MB 3.2s
2025-11-24T00:56:08.631327182Z #8 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 132.54MB / 132.54MB 8.4s
2025-11-24T00:56:09.231268446Z #8 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 13.14MB / 13.14MB 3.9s
2025-11-24T00:56:09.531179672Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 25.17MB / 158.61MB 4.7s
2025-11-24T00:56:09.865645677Z #8 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 2.10MB / 3.41MB 2.7s
2025-11-24T00:56:09.981455806Z #8 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 13.14MB / 13.14MB 4.5s done
2025-11-24T00:56:09.981469566Z #8 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 3.41MB / 3.41MB 2.9s
2025-11-24T00:56:10.133155088Z #8 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 3.41MB / 3.41MB 3.0s done
2025-11-24T00:56:10.902569307Z #8 extracting sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8
2025-11-24T00:56:11.066243124Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 41.94MB / 158.61MB 6.3s
2025-11-24T00:56:11.815958101Z #8 extracting sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 0.8s done
2025-11-24T00:56:11.815975222Z #8 extracting sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b
2025-11-24T00:56:11.966089313Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 58.72MB / 158.61MB 7.2s
2025-11-24T00:56:12.716099508Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 67.11MB / 158.61MB 8.0s
2025-11-24T00:56:12.86620413Z #8 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 132.54MB / 132.54MB 12.4s done
2025-11-24T00:56:12.86622406Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 83.89MB / 158.61MB 8.1s
2025-11-24T00:56:13.46636383Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 100.66MB / 158.61MB 8.7s
2025-11-24T00:56:14.515988813Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 109.05MB / 158.61MB 9.8s
2025-11-24T00:56:14.9656189Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 125.83MB / 158.61MB 10.2s
2025-11-24T00:56:15.415689429Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 134.22MB / 158.61MB 10.7s
2025-11-24T00:56:16.466296957Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 150.99MB / 158.61MB 11.7s
2025-11-24T00:56:18.862573014Z #8 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 158.61MB / 158.61MB 13.8s done
2025-11-24T00:56:28.374723241Z #8 ...
2025-11-24T00:56:28.374746261Z
2025-11-24T00:56:28.374751422Z #9 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T00:56:28.374755911Z #9 extracting sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 26.6s done
2025-11-24T00:56:28.374768242Z #9 DONE 42.9s
2025-11-24T00:56:28.525057208Z
2025-11-24T00:56:28.525105909Z #9 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T00:56:28.52511225Z #9 extracting sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a
2025-11-24T00:56:44.153743907Z #9 ...
2025-11-24T00:56:44.153760317Z
2025-11-24T00:56:44.153765097Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:56:44.153769248Z #8 extracting sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 32.4s done
2025-11-24T00:56:44.153773408Z #8 DONE 59.0s
2025-11-24T00:56:44.356080528Z
2025-11-24T00:56:44.356113209Z #9 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T00:56:44.356119199Z #9 extracting sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 15.8s done
2025-11-24T00:56:44.35612398Z #9 extracting sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 0.1s done
2025-11-24T00:56:44.35612749Z #9 extracting sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 0.1s done
2025-11-24T00:56:44.3561304Z #9 DONE 58.9s
2025-11-24T00:56:44.35613313Z
2025-11-24T00:56:44.35613774Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:56:44.35615402Z #8 extracting sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e
2025-11-24T00:56:44.733016945Z #8 ...
2025-11-24T00:56:44.733048556Z
2025-11-24T00:56:44.733055226Z #10 [stage-1 2/5] WORKDIR /app
2025-11-24T00:56:44.733059946Z #10 DONE 0.4s
2025-11-24T00:56:44.885724603Z
2025-11-24T00:56:44.885744173Z #11 [stage-1 3/5] RUN addgroup -S spring && adduser -S spring -G spring
2025-11-24T00:56:54.718693376Z #11 DONE 10.0s
2025-11-24T00:56:54.718720196Z
2025-11-24T00:56:54.718727547Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:56:54.900781842Z #8 ...
2025-11-24T00:56:54.900801232Z
2025-11-24T00:56:54.900806512Z #12 [stage-1 4/5] RUN mkdir -p /app/uploads && chown -R spring:spring /app
2025-11-24T00:57:00.152877648Z #12 ...
2025-11-24T00:57:00.152895838Z
2025-11-24T00:57:00.152903298Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:57:00.152909548Z #8 extracting sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 16.0s done
2025-11-24T00:57:00.152915279Z #8 DONE 75.0s
2025-11-24T00:57:00.303520633Z
2025-11-24T00:57:00.307133755Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:57:00.307146585Z #8 extracting sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454
2025-11-24T00:57:05.830659551Z #8 ...
2025-11-24T00:57:05.830676281Z
2025-11-24T00:57:05.830679981Z #12 [stage-1 4/5] RUN mkdir -p /app/uploads && chown -R spring:spring /app
2025-11-24T00:57:05.830683061Z #12 DONE 11.1s
2025-11-24T00:57:05.96807187Z
2025-11-24T00:57:05.96809805Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:57:05.96810955Z #8 extracting sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 5.7s done
2025-11-24T00:57:05.9681123Z #8 extracting sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 0.1s done
2025-11-24T00:57:05.96811482Z #8 extracting sha256:516905b70d0910e39f224893c8efc6801b1e1d053bf0031afdb3ef18728a4088 0.1s done
2025-11-24T00:57:05.968117811Z #8 DONE 80.8s
2025-11-24T00:57:06.118682414Z
2025-11-24T00:57:06.118706814Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:57:06.118711014Z #8 extracting sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd
2025-11-24T00:57:12.009082592Z #8 extracting sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 6.0s done
2025-11-24T00:57:12.009103972Z #8 DONE 86.9s
2025-11-24T00:57:12.159375508Z
2025-11-24T00:57:12.159396108Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:57:12.159402109Z #8 extracting sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96
2025-11-24T00:57:28.496942287Z #8 extracting sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 16.5s done
2025-11-24T00:57:28.496963708Z #8 DONE 103.3s
2025-11-24T00:57:28.679448023Z
2025-11-24T00:57:28.679478004Z #8 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T00:57:28.679484274Z #8 extracting sha256:1b2010acb2c2f72b7c96a939846c10d9796df96c67f60b271c88f62010e4b002 0.0s done
2025-11-24T00:57:28.679488584Z #8 DONE 103.4s
2025-11-24T00:57:28.679492424Z
2025-11-24T00:57:28.679497375Z #13 [builder 2/7] WORKDIR /app
2025-11-24T00:57:28.679501895Z #13 DONE 0.0s
2025-11-24T00:57:28.679505785Z
2025-11-24T00:57:28.679510535Z #14 [builder 3/7] COPY build.gradle settings.gradle gradlew ./
2025-11-24T00:57:28.679515225Z #14 DONE 0.1s
2025-11-24T00:57:28.832247414Z
2025-11-24T00:57:28.832272874Z #15 [builder 4/7] COPY gradle gradle/
2025-11-24T00:57:28.86744678Z #15 DONE 0.2s
2025-11-24T00:57:29.020706942Z
2025-11-24T00:57:29.020728882Z #16 [builder 5/7] RUN gradle dependencies --no-daemon || true
2025-11-24T00:57:29.918158301Z #16 1.048
2025-11-24T00:57:29.918189312Z #16 1.048 Welcome to Gradle 8.5!
2025-11-24T00:57:30.168521275Z #16 1.048
2025-11-24T00:57:30.168542295Z #16 1.048 Here are the highlights of this release:
2025-11-24T00:57:30.168593467Z #16 1.049  - Support for running on Java 21
2025-11-24T00:57:30.168600067Z #16 1.049  - Faster first use with Kotlin DSL
2025-11-24T00:57:30.168604827Z #16 1.049  - Improved error and warning messages
2025-11-24T00:57:30.168609217Z #16 1.049
2025-11-24T00:57:30.168613787Z #16 1.049 For more details see https://docs.gradle.org/8.5/release-notes.html
2025-11-24T00:57:30.168617877Z #16 1.049
2025-11-24T00:57:30.168622347Z #16 1.148 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
2025-11-24T00:57:31.517802887Z #16 2.648 Daemon will be stopped at the end of the build
2025-11-24T00:57:54.517099332Z #16 25.65
2025-11-24T00:57:54.66902299Z #16 25.65 > Task :dependencies
2025-11-24T00:57:54.669041061Z #16 25.65
2025-11-24T00:57:54.669046701Z #16 25.65 ------------------------------------------------------------
2025-11-24T00:57:54.669056881Z #16 25.65 Root project 'recipemate-api' - Recipe-based smart grocery group buying platform for students
2025-11-24T00:57:54.669071161Z #16 25.65 ------------------------------------------------------------
2025-11-24T00:57:54.669073542Z #16 25.65
2025-11-24T00:57:54.669075962Z #16 25.65 annotationProcessor - Annotation processors and their dependencies for source set 'main'.
2025-11-24T00:58:01.929288695Z #16 33.06 +--- org.projectlombok:lombok -> 1.18.42
2025-11-24T00:58:01.929319206Z #16 33.06 \--- io.github.openfeign.querydsl:querydsl-apt:7.0
2025-11-24T00:58:01.929324466Z #16 33.06      +--- io.github.openfeign.querydsl:querydsl-codegen:7.0
2025-11-24T00:58:01.929328336Z #16 33.06      |    +--- io.github.openfeign.querydsl:querydsl-core:7.0
2025-11-24T00:58:01.929332356Z #16 33.06      |    |    \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12
2025-11-24T00:58:01.929336476Z #16 33.06      |    |         \--- org.reactivestreams:reactive-streams:1.0.4
2025-11-24T00:58:01.929350327Z #16 33.06      |    +--- io.github.openfeign.querydsl:querydsl-codegen-utils:7.0
2025-11-24T00:58:01.929354467Z #16 33.06      |    |    +--- org.eclipse.jdt:ecj:3.40.0
2025-11-24T00:58:01.929358437Z #16 33.06      |    |    \--- io.github.classgraph:classgraph:4.8.179
2025-11-24T00:58:01.929362497Z #16 33.06      |    +--- jakarta.inject:jakarta.inject-api:2.0.1.MR -> 2.0.1
2025-11-24T00:58:01.929366537Z #16 33.06      |    +--- io.github.classgraph:classgraph:4.8.179
2025-11-24T00:58:01.929374627Z #16 33.06      |    \--- jakarta.annotation:jakarta.annotation-api:3.0.0 -> 2.1.1
2025-11-24T00:58:01.929385607Z #16 33.06      +--- jakarta.persistence:jakarta.persistence-api:3.2.0 -> 3.1.0
2025-11-24T00:58:01.929389828Z #16 33.06      \--- jakarta.annotation:jakarta.annotation-api:3.0.0 -> 2.1.1
2025-11-24T00:58:01.929393848Z #16 33.06
2025-11-24T00:58:01.929397638Z #16 33.06 bootArchives - Configuration for Spring Boot archive artifacts. (n)
2025-11-24T00:58:01.929402598Z #16 33.06 No dependencies
2025-11-24T00:58:01.929406538Z #16 33.06
2025-11-24T00:58:01.929410688Z #16 33.06 compileClasspath - Compile classpath for source set 'main'.
2025-11-24T00:58:09.627031358Z #16 40.76 +--- org.projectlombok:lombok -> 1.18.42
2025-11-24T00:58:09.627142391Z #16 40.76 +--- io.github.openfeign.querydsl:querydsl-apt:7.0
2025-11-24T00:58:09.627149991Z #16 40.76 |    +--- io.github.openfeign.querydsl:querydsl-codegen:7.0
2025-11-24T00:58:09.627154211Z #16 40.76 |    |    +--- io.github.openfeign.querydsl:querydsl-core:7.0
2025-11-24T00:58:09.627187392Z #16 40.76 |    |    |    \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12
2025-11-24T00:58:09.627192142Z #16 40.76 |    |    |         \--- org.reactivestreams:reactive-streams:1.0.4
2025-11-24T00:58:09.791731491Z #16 40.76 |    |    +--- io.github.openfeign.querydsl:querydsl-codegen-utils:7.0
2025-11-24T00:58:09.791766002Z #16 40.76 |    |    |    +--- org.eclipse.jdt:ecj:3.40.0
2025-11-24T00:58:09.791770682Z #16 40.76 |    |    |    \--- io.github.classgraph:classgraph:4.8.179
2025-11-24T00:58:09.791774722Z #16 40.76 |    |    +--- jakarta.inject:jakarta.inject-api:2.0.1.MR -> 2.0.1
2025-11-24T00:58:09.791778682Z #16 40.76 |    |    +--- io.github.classgraph:classgraph:4.8.179
2025-11-24T00:58:09.791783662Z #16 40.76 |    |    \--- jakarta.annotation:jakarta.annotation-api:3.0.0 -> 2.1.1
2025-11-24T00:58:09.791788012Z #16 40.76 |    +--- jakarta.persistence:jakarta.persistence-api:3.2.0 -> 3.1.0
2025-11-24T00:58:09.791814893Z #16 40.76 |    \--- jakarta.annotation:jakarta.annotation-api:3.0.0 -> 2.1.1
2025-11-24T00:58:09.791819423Z #16 40.76 +--- org.springframework.boot:spring-boot-starter-data-jpa -> 3.5.7
2025-11-24T00:58:09.791823313Z #16 40.76 |    +--- org.springframework.boot:spring-boot-starter:3.5.7
2025-11-24T00:58:09.791838474Z #16 40.76 |    |    +--- org.springframework.boot:spring-boot:3.5.7
2025-11-24T00:58:09.791841534Z #16 40.76 |    |    |    +--- org.springframework:spring-core:6.2.12
2025-11-24T00:58:09.791844394Z #16 40.76 |    |    |    |    \--- org.springframework:spring-jcl:6.2.12
2025-11-24T00:58:09.791846924Z #16 40.76 |    |    |    \--- org.springframework:spring-context:6.2.12
2025-11-24T00:58:09.791849164Z #16 40.76 |    |    |         +--- org.springframework:spring-aop:6.2.12
2025-11-24T00:58:09.791851324Z #16 40.76 |    |    |         |    +--- org.springframework:spring-beans:6.2.12
2025-11-24T00:58:09.791854014Z #16 40.76 |    |    |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.791891175Z #16 40.76 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.791893785Z #16 40.76 |    |    |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.791896225Z #16 40.76 |    |    |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.791898655Z #16 40.76 |    |    |         +--- org.springframework:spring-expression:6.2.12
2025-11-24T00:58:09.791901235Z #16 40.76 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.791903606Z #16 40.76 |    |    |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
2025-11-24T00:58:09.791906426Z #16 40.76 |    |    |              \--- io.micrometer:micrometer-commons:1.15.5
2025-11-24T00:58:09.791908706Z #16 40.76 |    |    +--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
2025-11-24T00:58:09.791911116Z #16 40.76 |    |    |    \--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:09.791913606Z #16 40.76 |    |    +--- org.springframework.boot:spring-boot-starter-logging:3.5.7
2025-11-24T00:58:09.791916396Z #16 40.76 |    |    |    +--- ch.qos.logback:logback-classic:1.5.20
2025-11-24T00:58:09.791918806Z #16 40.76 |    |    |    |    +--- ch.qos.logback:logback-core:1.5.20
2025-11-24T00:58:09.791921356Z #16 40.76 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:09.791923816Z #16 40.76 |    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.24.3
2025-11-24T00:58:09.791926246Z #16 40.76 |    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.24.3
2025-11-24T00:58:09.791929176Z #16 40.76 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:09.791931846Z #16 40.76 |    |    |    \--- org.slf4j:jul-to-slf4j:2.0.17
2025-11-24T00:58:09.791934406Z #16 40.76 |    |    |         \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:09.791936806Z #16 40.76 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
2025-11-24T00:58:09.791939156Z #16 40.76 |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.791942106Z #16 40.76 |    |    \--- org.yaml:snakeyaml:2.4
2025-11-24T00:58:09.791944656Z #16 40.76 |    +--- org.springframework.boot:spring-boot-starter-jdbc:3.5.7
2025-11-24T00:58:09.791951067Z #16 40.76 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:09.791953677Z #16 40.76 |    |    +--- com.zaxxer:HikariCP:6.3.3
2025-11-24T00:58:09.791956137Z #16 40.76 |    |    |    \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:09.791958657Z #16 40.76 |    |    \--- org.springframework:spring-jdbc:6.2.12
2025-11-24T00:58:09.791961107Z #16 40.76 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.791963577Z #16 40.76 |    |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.791965877Z #16 40.76 |    |         \--- org.springframework:spring-tx:6.2.12
2025-11-24T00:58:09.791968247Z #16 40.76 |    |              +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.791975767Z #16 40.76 |    |              \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.791980657Z #16 40.76 |    +--- org.hibernate.orm:hibernate-core:6.6.33.Final
2025-11-24T00:58:09.791983248Z #16 40.76 |    |    +--- jakarta.persistence:jakarta.persistence-api:3.1.0
2025-11-24T00:58:09.791985797Z #16 40.76 |    |    \--- jakarta.transaction:jakarta.transaction-api:2.0.1
2025-11-24T00:58:09.791988417Z #16 40.76 |    +--- org.springframework.data:spring-data-jpa:3.5.5
2025-11-24T00:58:09.791990818Z #16 40.76 |    |    +--- org.springframework.data:spring-data-commons:3.5.5
2025-11-24T00:58:09.791993278Z #16 40.76 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.791995858Z #16 40.76 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.791998228Z #16 40.76 |    |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:09.792000648Z #16 40.76 |    |    +--- org.springframework:spring-orm:6.2.12
2025-11-24T00:58:09.792003048Z #16 40.76 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.792005558Z #16 40.76 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.792008008Z #16 40.76 |    |    |    +--- org.springframework:spring-jdbc:6.2.12 (*)
2025-11-24T00:58:09.792010318Z #16 40.76 |    |    |    \--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:09.792012878Z #16 40.76 |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:09.792015378Z #16 40.76 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:09.792017818Z #16 40.76 |    |    +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:09.792020318Z #16 40.76 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.792022769Z #16 40.76 |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.792025198Z #16 40.76 |    |    +--- org.antlr:antlr4-runtime:4.13.0
2025-11-24T00:58:09.792027578Z #16 40.76 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.0.0 -> 2.1.1
2025-11-24T00:58:09.792029979Z #16 40.76 |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:09.792032359Z #16 40.76 |    \--- org.springframework:spring-aspects:6.2.12
2025-11-24T00:58:09.792034919Z #16 40.76 |         \--- org.aspectj:aspectjweaver:1.9.22.1 -> 1.9.24
2025-11-24T00:58:09.792037379Z #16 40.76 +--- org.springframework.boot:spring-boot-starter-security -> 3.5.7
2025-11-24T00:58:09.792039759Z #16 40.76 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:09.792042239Z #16 40.76 |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:09.792044929Z #16 40.76 |    +--- org.springframework.security:spring-security-config:6.5.6
2025-11-24T00:58:09.792047319Z #16 40.76 |    |    +--- org.springframework.security:spring-security-core:6.5.6
2025-11-24T00:58:09.792049759Z #16 40.76 |    |    |    +--- org.springframework.security:spring-security-crypto:6.5.6
2025-11-24T00:58:09.792054359Z #16 40.76 |    |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:09.792057219Z #16 40.76 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.792059569Z #16 40.76 |    |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:09.792061959Z #16 40.76 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.79206445Z #16 40.76 |    |    |    +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:09.792066599Z #16 40.76 |    |    |    \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
2025-11-24T00:58:09.79207345Z #16 40.76 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:09.79207597Z #16 40.76 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.79207845Z #16 40.76 |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:09.79208103Z #16 40.76 |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.79208334Z #16 40.76 |    \--- org.springframework.security:spring-security-web:6.5.6
2025-11-24T00:58:09.79208604Z #16 40.76 |         +--- org.springframework.security:spring-security-core:6.5.6 (*)
2025-11-24T00:58:09.79208839Z #16 40.76 |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.7920908Z #16 40.76 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:09.79209318Z #16 40.76 |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.79209569Z #16 40.76 |         +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:09.79209812Z #16 40.76 |         +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:09.792100691Z #16 40.76 |         \--- org.springframework:spring-web:6.2.12
2025-11-24T00:58:09.792103211Z #16 40.76 |              +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.792117581Z #16 40.76 |              +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.792120211Z #16 40.76 |              \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
2025-11-24T00:58:09.792122551Z #16 40.76 +--- org.springframework.boot:spring-boot-starter-thymeleaf -> 3.5.7
2025-11-24T00:58:09.792125061Z #16 40.76 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:09.792127571Z #16 40.76 |    \--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE
2025-11-24T00:58:09.792130111Z #16 40.76 |         +--- org.thymeleaf:thymeleaf:3.1.3.RELEASE
2025-11-24T00:58:09.792132591Z #16 40.77 |         |    +--- org.attoparser:attoparser:2.0.7.RELEASE
2025-11-24T00:58:09.792135071Z #16 40.77 |         |    +--- org.unbescape:unbescape:1.1.6.RELEASE
2025-11-24T00:58:09.792137381Z #16 40.77 |         |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:09.792139941Z #16 40.77 |         \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:09.792144032Z #16 40.77 +--- org.springframework.boot:spring-boot-starter-validation -> 3.5.7
2025-11-24T00:58:09.792146572Z #16 40.77 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:09.792149032Z #16 40.77 |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
2025-11-24T00:58:09.792151792Z #16 40.77 |    \--- org.hibernate.validator:hibernate-validator:8.0.3.Final
2025-11-24T00:58:09.792154262Z #16 40.77 |         +--- jakarta.validation:jakarta.validation-api:3.0.2
2025-11-24T00:58:09.792156892Z #16 40.77 |         +--- org.jboss.logging:jboss-logging:3.4.3.Final -> 3.6.1.Final
2025-11-24T00:58:09.792159362Z #16 40.77 |         \--- com.fasterxml:classmate:1.5.1 -> 1.7.1
2025-11-24T00:58:09.792161792Z #16 40.77 +--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
2025-11-24T00:58:09.792164412Z #16 40.77 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:09.792167352Z #16 40.77 |    +--- org.springframework.boot:spring-boot-starter-json:3.5.7
2025-11-24T00:58:09.792169632Z #16 40.77 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:09.792172022Z #16 40.77 |    |    +--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:09.792174592Z #16 40.77 |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2
2025-11-24T00:58:09.792177312Z #16 40.77 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2
2025-11-24T00:58:09.792184862Z #16 40.77 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2
2025-11-24T00:58:09.792187523Z #16 40.77 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (c)
2025-11-24T00:58:09.792189273Z #16 40.77 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (c)
2025-11-24T00:58:09.792191023Z #16 40.77 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (c)
2025-11-24T00:58:09.792192803Z #16 40.77 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2 (c)
2025-11-24T00:58:09.792194453Z #16 40.77 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2 (c)
2025-11-24T00:58:09.792196203Z #16 40.77 |    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2 (c)
2025-11-24T00:58:09.792197863Z #16 40.77 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2
2025-11-24T00:58:09.792199513Z #16 40.77 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:09.792201153Z #16 40.77 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:09.792202823Z #16 40.77 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
2025-11-24T00:58:09.792204553Z #16 40.77 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:09.792206603Z #16 40.77 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:09.792208273Z #16 40.77 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:09.792209943Z #16 40.77 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
2025-11-24T00:58:09.792211603Z #16 40.77 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (*)
2025-11-24T00:58:09.792213253Z #16 40.77 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:09.792214913Z #16 40.77 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:09.792216903Z #16 40.77 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:09.792218563Z #16 40.77 |    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
2025-11-24T00:58:09.792220314Z #16 40.77 |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:09.792221963Z #16 40.77 |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:09.792223634Z #16 40.77 |    |         \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:09.792225294Z #16 40.77 |    +--- org.springframework.boot:spring-boot-starter-tomcat:3.5.7
2025-11-24T00:58:09.792226964Z #16 40.77 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
2025-11-24T00:58:09.792228634Z #16 40.77 |    |    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
2025-11-24T00:58:09.792230344Z #16 40.77 |    |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
2025-11-24T00:58:09.792232034Z #16 40.77 |    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.48
2025-11-24T00:58:09.792233734Z #16 40.77 |    |         \--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
2025-11-24T00:58:09.792235474Z #16 40.77 |    +--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:09.792237184Z #16 40.77 |    \--- org.springframework:spring-webmvc:6.2.12
2025-11-24T00:58:09.792238894Z #16 40.77 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:09.792240584Z #16 40.77 |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.792245014Z #16 40.77 |         +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:09.792246754Z #16 40.77 |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.792248414Z #16 40.77 |         +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:09.792250104Z #16 40.77 |         \--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:09.792260384Z #16 40.77 +--- org.springframework.retry:spring-retry -> 2.0.12
2025-11-24T00:58:09.792262275Z #16 40.77 +--- org.springframework:spring-aspects -> 6.2.12 (*)
2025-11-24T00:58:09.792263964Z #16 40.77 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 -> 3.1.3.RELEASE
2025-11-24T00:58:09.792265655Z #16 40.77 |    +--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE (*)
2025-11-24T00:58:09.792267415Z #16 40.77 |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:09.792269105Z #16 40.77 +--- org.springframework.boot:spring-boot-starter-data-redis -> 3.5.7
2025-11-24T00:58:09.792270805Z #16 40.77 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:09.792274335Z #16 40.77 |    +--- io.lettuce:lettuce-core:6.6.0.RELEASE
2025-11-24T00:58:09.792276085Z #16 40.77 |    |    +--- redis.clients.authentication:redis-authx-core:0.1.1-beta2
2025-11-24T00:58:09.792277775Z #16 40.77 |    |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
2025-11-24T00:58:09.792279445Z #16 40.77 |    |    +--- io.netty:netty-common:4.1.118.Final -> 4.1.128.Final
2025-11-24T00:58:09.792281095Z #16 40.77 |    |    +--- io.netty:netty-handler:4.1.118.Final -> 4.1.128.Final
2025-11-24T00:58:09.792283925Z #16 40.77 |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:09.792285635Z #16 40.77 |    |    |    +--- io.netty:netty-resolver:4.1.128.Final
2025-11-24T00:58:09.792287335Z #16 40.77 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:09.792288995Z #16 40.77 |    |    |    +--- io.netty:netty-buffer:4.1.128.Final
2025-11-24T00:58:09.792290705Z #16 40.77 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:09.792292385Z #16 40.77 |    |    |    +--- io.netty:netty-transport:4.1.128.Final
2025-11-24T00:58:09.792294065Z #16 40.77 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:09.792295725Z #16 40.77 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:09.792297385Z #16 40.77 |    |    |    |    \--- io.netty:netty-resolver:4.1.128.Final (*)
2025-11-24T00:58:09.792299056Z #16 40.77 |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.128.Final
2025-11-24T00:58:09.792300745Z #16 40.77 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:09.792302436Z #16 40.77 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:09.792304116Z #16 40.77 |    |    |    |    \--- io.netty:netty-transport:4.1.128.Final (*)
2025-11-24T00:58:09.792305816Z #16 40.77 |    |    |    \--- io.netty:netty-codec:4.1.128.Final
2025-11-24T00:58:09.792307506Z #16 40.77 |    |    |         +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:09.792309176Z #16 40.77 |    |    |         +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:09.792310866Z #16 40.77 |    |    |         \--- io.netty:netty-transport:4.1.128.Final (*)
2025-11-24T00:58:09.792312536Z #16 40.77 |    |    +--- io.netty:netty-transport:4.1.118.Final -> 4.1.128.Final (*)
2025-11-24T00:58:09.792314216Z #16 40.77 |    |    \--- io.projectreactor:reactor-core:3.6.6 -> 3.7.12 (*)
2025-11-24T00:58:09.792315866Z #16 40.77 |    \--- org.springframework.data:spring-data-redis:3.5.5
2025-11-24T00:58:09.792317526Z #16 40.77 |         +--- org.springframework.data:spring-data-keyvalue:3.5.5
2025-11-24T00:58:09.792321536Z #16 40.77 |         |    +--- org.springframework.data:spring-data-commons:3.5.5 (*)
2025-11-24T00:58:09.792323246Z #16 40.77 |         |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:09.792324906Z #16 40.77 |         |    +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:09.792326596Z #16 40.77 |         |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:09.792328256Z #16 40.77 |         +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:09.792329986Z #16 40.77 |         +--- org.springframework:spring-oxm:6.2.12
2025-11-24T00:58:09.792331666Z #16 40.77 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.792333386Z #16 40.77 |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.792335056Z #16 40.77 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:09.792336726Z #16 40.77 |         +--- org.springframework:spring-context-support:6.2.12
2025-11-24T00:58:09.792338377Z #16 40.77 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:09.792340046Z #16 40.77 |         |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:09.792341717Z #16 40.77 |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:09.792343457Z #16 40.77 |         \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:09.792345117Z #16 40.77 +--- org.springframework.boot:spring-boot-starter-cache -> 3.5.7
2025-11-24T00:58:09.792346787Z #16 40.77 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:09.792348507Z #16 40.77 |    \--- org.springframework:spring-context-support:6.2.12 (*)
2025-11-24T00:58:09.792359917Z #16 40.77 +--- net.coobird:thumbnailator:0.4.19
2025-11-24T00:58:09.792363007Z #16 40.77 +--- com.cloudinary:cloudinary-http5:2.3.0
2025-11-24T00:58:09.792365547Z #16 40.77 |    +--- com.cloudinary:cloudinary-core:2.3.0
2025-11-24T00:58:09.792367837Z #16 40.77 |    +--- org.apache.commons:commons-lang3:3.1 -> 3.18.0
2025-11-24T00:58:09.792370077Z #16 40.77 |    +--- org.apache.httpcomponents.client5:httpclient5:5.3.1 -> 5.5.1
2025-11-24T00:58:09.792372487Z #16 40.77 |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.3.6
2025-11-24T00:58:09.792374797Z #16 40.77 |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.3.6
2025-11-24T00:58:09.792377027Z #16 40.77 |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.3.6
2025-11-24T00:58:09.792379247Z #16 40.77 |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
2025-11-24T00:58:09.792381367Z #16 40.77 |    \--- org.apache.httpcomponents.core5:httpcore5:5.2.5 -> 5.3.6
2025-11-24T00:58:09.792383528Z #16 40.77 \--- io.github.openfeign.querydsl:querydsl-jpa:7.0
2025-11-24T00:58:09.792385668Z #16 40.77      \--- io.github.openfeign.querydsl:querydsl-core:7.0 (*)
2025-11-24T00:58:09.792387798Z #16 40.77
2025-11-24T00:58:09.792389968Z #16 40.77 compileOnly - Compile-only dependencies for the 'main' feature. (n)
2025-11-24T00:58:09.792392588Z #16 40.77 \--- org.projectlombok:lombok (n)
2025-11-24T00:58:09.792394698Z #16 40.77
2025-11-24T00:58:09.792396828Z #16 40.77 default - Configuration for default artifacts. (n)
2025-11-24T00:58:09.792399508Z #16 40.77 No dependencies
2025-11-24T00:58:09.792401688Z #16 40.77
2025-11-24T00:58:09.792403928Z #16 40.77 developmentOnly - Configuration for development-only dependencies such as Spring Boot's DevTools.
2025-11-24T00:58:10.028179141Z #16 40.95 \--- org.springframework.boot:spring-boot-devtools -> 3.5.7
2025-11-24T00:58:10.028198531Z #16 40.95      +--- org.springframework.boot:spring-boot:3.5.7
2025-11-24T00:58:10.028212942Z #16 40.95      |    +--- org.springframework:spring-core:6.2.12
2025-11-24T00:58:10.028216352Z #16 40.95      |    |    \--- org.springframework:spring-jcl:6.2.12
2025-11-24T00:58:10.028219332Z #16 40.95      |    \--- org.springframework:spring-context:6.2.12
2025-11-24T00:58:10.028221872Z #16 40.95      |         +--- org.springframework:spring-aop:6.2.12
2025-11-24T00:58:10.028224682Z #16 40.95      |         |    +--- org.springframework:spring-beans:6.2.12
2025-11-24T00:58:10.028228182Z #16 40.95      |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.028230862Z #16 40.95      |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.028233472Z #16 40.95      |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.028236262Z #16 40.95      |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.028239062Z #16 40.95      |         +--- org.springframework:spring-expression:6.2.12
2025-11-24T00:58:10.028241903Z #16 40.95      |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.028244732Z #16 40.95      |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
2025-11-24T00:58:10.028247423Z #16 40.95      |              \--- io.micrometer:micrometer-commons:1.15.5
2025-11-24T00:58:10.028250063Z #16 40.95      \--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
2025-11-24T00:58:10.028252953Z #16 40.95           \--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:10.028255483Z #16 40.95
2025-11-24T00:58:10.028258283Z #16 40.95 implementation - Implementation dependencies for the 'main' feature. (n)
2025-11-24T00:58:10.028260893Z #16 40.95 +--- org.springframework.boot:spring-boot-starter-data-jpa (n)
2025-11-24T00:58:10.028263403Z #16 40.95 +--- org.springframework.boot:spring-boot-starter-security (n)
2025-11-24T00:58:10.028265923Z #16 40.95 +--- org.springframework.boot:spring-boot-starter-thymeleaf (n)
2025-11-24T00:58:10.028268443Z #16 40.95 +--- org.springframework.boot:spring-boot-starter-validation (n)
2025-11-24T00:58:10.028270823Z #16 40.95 +--- org.springframework.boot:spring-boot-starter-web (n)
2025-11-24T00:58:10.028273223Z #16 40.95 +--- org.springframework.retry:spring-retry (n)
2025-11-24T00:58:10.028275903Z #16 40.95 +--- org.springframework:spring-aspects (n)
2025-11-24T00:58:10.028278293Z #16 40.95 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 (n)
2025-11-24T00:58:10.028280853Z #16 40.95 +--- org.springframework.boot:spring-boot-starter-data-redis (n)
2025-11-24T00:58:10.028283504Z #16 40.95 +--- org.springframework.boot:spring-boot-starter-cache (n)
2025-11-24T00:58:10.028286284Z #16 40.95 +--- net.coobird:thumbnailator:0.4.19 (n)
2025-11-24T00:58:10.028288704Z #16 40.95 +--- com.cloudinary:cloudinary-http5:2.3.0 (n)
2025-11-24T00:58:10.028291014Z #16 40.95 \--- io.github.openfeign.querydsl:querydsl-jpa:7.0 (n)
2025-11-24T00:58:10.028293194Z #16 40.95
2025-11-24T00:58:10.028295484Z #16 40.95 mainSourceElements - List of source directories contained in the Main SourceSet. (n)
2025-11-24T00:58:10.028298404Z #16 40.95 No dependencies
2025-11-24T00:58:10.028301044Z #16 40.95
2025-11-24T00:58:10.028303924Z #16 40.95 productionRuntimeClasspath
2025-11-24T00:58:10.128633628Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-data-jpa -> 3.5.7
2025-11-24T00:58:10.129424339Z #16 41.26 |    +--- org.springframework.boot:spring-boot-starter:3.5.7
2025-11-24T00:58:10.129436669Z #16 41.26 |    |    +--- org.springframework.boot:spring-boot:3.5.7
2025-11-24T00:58:10.129439749Z #16 41.26 |    |    |    +--- org.springframework:spring-core:6.2.12
2025-11-24T00:58:10.129451859Z #16 41.26 |    |    |    |    \--- org.springframework:spring-jcl:6.2.12
2025-11-24T00:58:10.129454639Z #16 41.26 |    |    |    \--- org.springframework:spring-context:6.2.12
2025-11-24T00:58:10.12948455Z #16 41.26 |    |    |         +--- org.springframework:spring-aop:6.2.12
2025-11-24T00:58:10.12948803Z #16 41.26 |    |    |         |    +--- org.springframework:spring-beans:6.2.12
2025-11-24T00:58:10.12949127Z #16 41.26 |    |    |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.12949413Z #16 41.26 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.12949662Z #16 41.26 |    |    |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.129499021Z #16 41.26 |    |    |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129501421Z #16 41.26 |    |    |         +--- org.springframework:spring-expression:6.2.12
2025-11-24T00:58:10.129507151Z #16 41.26 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129509961Z #16 41.26 |    |    |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
2025-11-24T00:58:10.129512411Z #16 41.26 |    |    |              \--- io.micrometer:micrometer-commons:1.15.5
2025-11-24T00:58:10.129514751Z #16 41.26 |    |    +--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
2025-11-24T00:58:10.129517231Z #16 41.26 |    |    |    \--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:10.129519611Z #16 41.26 |    |    +--- org.springframework.boot:spring-boot-starter-logging:3.5.7
2025-11-24T00:58:10.129521981Z #16 41.26 |    |    |    +--- ch.qos.logback:logback-classic:1.5.20
2025-11-24T00:58:10.129524521Z #16 41.26 |    |    |    |    +--- ch.qos.logback:logback-core:1.5.20
2025-11-24T00:58:10.129527541Z #16 41.26 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:10.129574712Z #16 41.26 |    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.24.3
2025-11-24T00:58:10.129577763Z #16 41.26 |    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.24.3
2025-11-24T00:58:10.129580112Z #16 41.26 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:10.129582612Z #16 41.26 |    |    |    \--- org.slf4j:jul-to-slf4j:2.0.17
2025-11-24T00:58:10.129584923Z #16 41.26 |    |    |         \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:10.129587173Z #16 41.26 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
2025-11-24T00:58:10.129589513Z #16 41.26 |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129592573Z #16 41.26 |    |    \--- org.yaml:snakeyaml:2.4
2025-11-24T00:58:10.129594723Z #16 41.26 |    +--- org.springframework.boot:spring-boot-starter-jdbc:3.5.7
2025-11-24T00:58:10.129596973Z #16 41.26 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.129599283Z #16 41.26 |    |    +--- com.zaxxer:HikariCP:6.3.3
2025-11-24T00:58:10.129601653Z #16 41.26 |    |    |    \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:10.129603943Z #16 41.26 |    |    \--- org.springframework:spring-jdbc:6.2.12
2025-11-24T00:58:10.129606153Z #16 41.26 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.129610863Z #16 41.26 |    |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129613133Z #16 41.26 |    |         \--- org.springframework:spring-tx:6.2.12
2025-11-24T00:58:10.129627974Z #16 41.26 |    |              +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.129630354Z #16 41.26 |    |              \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129638574Z #16 41.26 |    +--- org.hibernate.orm:hibernate-core:6.6.33.Final
2025-11-24T00:58:10.129641334Z #16 41.26 |    |    +--- jakarta.persistence:jakarta.persistence-api:3.1.0
2025-11-24T00:58:10.129643624Z #16 41.26 |    |    +--- jakarta.transaction:jakarta.transaction-api:2.0.1
2025-11-24T00:58:10.129645984Z #16 41.26 |    |    +--- org.jboss.logging:jboss-logging:3.5.0.Final -> 3.6.1.Final
2025-11-24T00:58:10.129648124Z #16 41.26 |    |    +--- org.hibernate.common:hibernate-commons-annotations:7.0.3.Final
2025-11-24T00:58:10.129650224Z #16 41.26 |    |    +--- io.smallrye:jandex:3.2.0
2025-11-24T00:58:10.129652414Z #16 41.26 |    |    +--- com.fasterxml:classmate:1.5.1 -> 1.7.1
2025-11-24T00:58:10.129654484Z #16 41.26 |    |    +--- net.bytebuddy:byte-buddy:1.15.11 -> 1.17.8
2025-11-24T00:58:10.129656654Z #16 41.26 |    |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.0 -> 4.0.4
2025-11-24T00:58:10.129658794Z #16 41.26 |    |    |    \--- jakarta.activation:jakarta.activation-api:2.1.4
2025-11-24T00:58:10.129661054Z #16 41.26 |    |    +--- org.glassfish.jaxb:jaxb-runtime:4.0.2 -> 4.0.6
2025-11-24T00:58:10.129663555Z #16 41.26 |    |    |    \--- org.glassfish.jaxb:jaxb-core:4.0.6
2025-11-24T00:58:10.129665805Z #16 41.26 |    |    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.4 (*)
2025-11-24T00:58:10.129667955Z #16 41.26 |    |    |         +--- jakarta.activation:jakarta.activation-api:2.1.4
2025-11-24T00:58:10.129670035Z #16 41.26 |    |    |         +--- org.eclipse.angus:angus-activation:2.0.3
2025-11-24T00:58:10.129672285Z #16 41.26 |    |    |         |    \--- jakarta.activation:jakarta.activation-api:2.1.4
2025-11-24T00:58:10.129692205Z #16 41.26 |    |    |         +--- org.glassfish.jaxb:txw2:4.0.6
2025-11-24T00:58:10.129694995Z #16 41.26 |    |    |         \--- com.sun.istack:istack-commons-runtime:4.1.2
2025-11-24T00:58:10.129697386Z #16 41.26 |    |    +--- jakarta.inject:jakarta.inject-api:2.0.1
2025-11-24T00:58:10.129699535Z #16 41.26 |    |    \--- org.antlr:antlr4-runtime:4.13.0
2025-11-24T00:58:10.129701686Z #16 41.26 |    +--- org.springframework.data:spring-data-jpa:3.5.5
2025-11-24T00:58:10.129703916Z #16 41.26 |    |    +--- org.springframework.data:spring-data-commons:3.5.5
2025-11-24T00:58:10.129706116Z #16 41.26 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129708246Z #16 41.26 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.129710306Z #16 41.26 |    |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:10.129712406Z #16 41.26 |    |    +--- org.springframework:spring-orm:6.2.12
2025-11-24T00:58:10.129714496Z #16 41.26 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.129716566Z #16 41.26 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129718646Z #16 41.26 |    |    |    +--- org.springframework:spring-jdbc:6.2.12 (*)
2025-11-24T00:58:10.129720776Z #16 41.26 |    |    |    \--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:10.129722886Z #16 41.26 |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.129725066Z #16 41.26 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.129727146Z #16 41.26 |    |    +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:10.129729266Z #16 41.26 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.129731326Z #16 41.26 |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129733446Z #16 41.26 |    |    +--- org.antlr:antlr4-runtime:4.13.0
2025-11-24T00:58:10.129735516Z #16 41.26 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.0.0 -> 2.1.1
2025-11-24T00:58:10.129764327Z #16 41.26 |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:10.129767127Z #16 41.26 |    \--- org.springframework:spring-aspects:6.2.12
2025-11-24T00:58:10.129769407Z #16 41.26 |         \--- org.aspectj:aspectjweaver:1.9.22.1 -> 1.9.24
2025-11-24T00:58:10.129771637Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-security -> 3.5.7
2025-11-24T00:58:10.129773717Z #16 41.26 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.129775888Z #16 41.26 |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.129777977Z #16 41.26 |    +--- org.springframework.security:spring-security-config:6.5.6
2025-11-24T00:58:10.129780068Z #16 41.26 |    |    +--- org.springframework.security:spring-security-core:6.5.6
2025-11-24T00:58:10.129782188Z #16 41.26 |    |    |    +--- org.springframework.security:spring-security-crypto:6.5.6
2025-11-24T00:58:10.129784408Z #16 41.26 |    |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.129786418Z #16 41.26 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.129788528Z #16 41.26 |    |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.129790678Z #16 41.26 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129792708Z #16 41.26 |    |    |    +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:10.129794888Z #16 41.26 |    |    |    \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
2025-11-24T00:58:10.129797388Z #16 41.26 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.129800608Z #16 41.26 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.129803148Z #16 41.26 |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.129805638Z #16 41.26 |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129808048Z #16 41.26 |    \--- org.springframework.security:spring-security-web:6.5.6
2025-11-24T00:58:10.129810498Z #16 41.26 |         +--- org.springframework.security:spring-security-core:6.5.6 (*)
2025-11-24T00:58:10.129812889Z #16 41.26 |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129815109Z #16 41.26 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.129817389Z #16 41.26 |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.129819629Z #16 41.26 |         +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.129821859Z #16 41.26 |         +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:10.129824389Z #16 41.26 |         \--- org.springframework:spring-web:6.2.12
2025-11-24T00:58:10.129826849Z #16 41.26 |              +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.129829269Z #16 41.26 |              +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.129831789Z #16 41.26 |              \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
2025-11-24T00:58:10.129834119Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-thymeleaf -> 3.5.7
2025-11-24T00:58:10.129836619Z #16 41.26 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.129839159Z #16 41.26 |    \--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE
2025-11-24T00:58:10.129841689Z #16 41.26 |         +--- org.thymeleaf:thymeleaf:3.1.3.RELEASE
2025-11-24T00:58:10.129844119Z #16 41.26 |         |    +--- org.attoparser:attoparser:2.0.7.RELEASE
2025-11-24T00:58:10.129846639Z #16 41.26 |         |    +--- org.unbescape:unbescape:1.1.6.RELEASE
2025-11-24T00:58:10.12988014Z #16 41.26 |         |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:10.12988671Z #16 41.26 |         \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:10.12988923Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-validation -> 3.5.7
2025-11-24T00:58:10.12989162Z #16 41.26 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.12989382Z #16 41.26 |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
2025-11-24T00:58:10.129896191Z #16 41.26 |    \--- org.hibernate.validator:hibernate-validator:8.0.3.Final
2025-11-24T00:58:10.129898491Z #16 41.26 |         +--- jakarta.validation:jakarta.validation-api:3.0.2
2025-11-24T00:58:10.129900781Z #16 41.26 |         +--- org.jboss.logging:jboss-logging:3.4.3.Final -> 3.6.1.Final
2025-11-24T00:58:10.129903081Z #16 41.26 |         \--- com.fasterxml:classmate:1.5.1 -> 1.7.1
2025-11-24T00:58:10.129905441Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
2025-11-24T00:58:10.129907781Z #16 41.26 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.129909891Z #16 41.26 |    +--- org.springframework.boot:spring-boot-starter-json:3.5.7
2025-11-24T00:58:10.129912091Z #16 41.26 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.129914281Z #16 41.26 |    |    +--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:10.129916631Z #16 41.26 |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2
2025-11-24T00:58:10.129918991Z #16 41.26 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2
2025-11-24T00:58:10.129921261Z #16 41.26 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2
2025-11-24T00:58:10.129924471Z #16 41.26 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (c)
2025-11-24T00:58:10.129927051Z #16 41.26 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (c)
2025-11-24T00:58:10.129929471Z #16 41.26 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (c)
2025-11-24T00:58:10.129931961Z #16 41.26 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2 (c)
2025-11-24T00:58:10.129934221Z #16 41.26 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2 (c)
2025-11-24T00:58:10.129936632Z #16 41.26 |    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2 (c)
2025-11-24T00:58:10.129938992Z #16 41.26 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2
2025-11-24T00:58:10.129941162Z #16 41.26 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:10.129943512Z #16 41.26 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:10.129945662Z #16 41.26 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
2025-11-24T00:58:10.129947942Z #16 41.26 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:10.129950442Z #16 41.26 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:10.129952812Z #16 41.26 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:10.129954992Z #16 41.26 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
2025-11-24T00:58:10.129957422Z #16 41.26 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (*)
2025-11-24T00:58:10.129959792Z #16 41.26 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:10.129962142Z #16 41.26 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:10.129969853Z #16 41.26 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:10.129972282Z #16 41.26 |    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
2025-11-24T00:58:10.129974653Z #16 41.26 |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:10.129976773Z #16 41.26 |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:10.129979113Z #16 41.26 |    |         \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:10.129981163Z #16 41.26 |    +--- org.springframework.boot:spring-boot-starter-tomcat:3.5.7
2025-11-24T00:58:10.129983403Z #16 41.26 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
2025-11-24T00:58:10.129985893Z #16 41.26 |    |    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
2025-11-24T00:58:10.129988493Z #16 41.26 |    |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
2025-11-24T00:58:10.129990883Z #16 41.26 |    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.48
2025-11-24T00:58:10.129993163Z #16 41.26 |    |         \--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
2025-11-24T00:58:10.129995593Z #16 41.26 |    +--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:10.129997623Z #16 41.26 |    \--- org.springframework:spring-webmvc:6.2.12
2025-11-24T00:58:10.129999853Z #16 41.26 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.130002113Z #16 41.26 |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.130004533Z #16 41.26 |         +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.130006993Z #16 41.26 |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.130017624Z #16 41.26 |         +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:10.130020194Z #16 41.26 |         \--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:10.130022804Z #16 41.26 +--- org.springframework.retry:spring-retry -> 2.0.12
2025-11-24T00:58:10.130025144Z #16 41.26 +--- org.springframework:spring-aspects -> 6.2.12 (*)
2025-11-24T00:58:10.130027454Z #16 41.26 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 -> 3.1.3.RELEASE
2025-11-24T00:58:10.130029864Z #16 41.26 |    +--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE (*)
2025-11-24T00:58:10.130032384Z #16 41.26 |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:10.130036594Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-data-redis -> 3.5.7
2025-11-24T00:58:10.130039434Z #16 41.26 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.130041674Z #16 41.26 |    +--- io.lettuce:lettuce-core:6.6.0.RELEASE
2025-11-24T00:58:10.130043844Z #16 41.26 |    |    +--- redis.clients.authentication:redis-authx-core:0.1.1-beta2
2025-11-24T00:58:10.130046194Z #16 41.26 |    |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
2025-11-24T00:58:10.130048704Z #16 41.26 |    |    +--- io.netty:netty-common:4.1.118.Final -> 4.1.128.Final
2025-11-24T00:58:10.289167806Z #16 41.26 |    |    +--- io.netty:netty-handler:4.1.118.Final -> 4.1.128.Final
2025-11-24T00:58:10.289184726Z #16 41.26 |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.289187466Z #16 41.26 |    |    |    +--- io.netty:netty-resolver:4.1.128.Final
2025-11-24T00:58:10.289189896Z #16 41.26 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.289191986Z #16 41.26 |    |    |    +--- io.netty:netty-buffer:4.1.128.Final
2025-11-24T00:58:10.289193966Z #16 41.26 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.289204496Z #16 41.26 |    |    |    +--- io.netty:netty-transport:4.1.128.Final
2025-11-24T00:58:10.289206927Z #16 41.26 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.289209157Z #16 41.26 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:10.289211217Z #16 41.26 |    |    |    |    \--- io.netty:netty-resolver:4.1.128.Final (*)
2025-11-24T00:58:10.289214137Z #16 41.26 |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.128.Final
2025-11-24T00:58:10.289216517Z #16 41.26 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.289218557Z #16 41.26 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:10.289220637Z #16 41.26 |    |    |    |    \--- io.netty:netty-transport:4.1.128.Final (*)
2025-11-24T00:58:10.289223067Z #16 41.26 |    |    |    \--- io.netty:netty-codec:4.1.128.Final
2025-11-24T00:58:10.289225137Z #16 41.26 |    |    |         +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.289227207Z #16 41.26 |    |    |         +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:10.289229267Z #16 41.26 |    |    |         \--- io.netty:netty-transport:4.1.128.Final (*)
2025-11-24T00:58:10.289231477Z #16 41.26 |    |    +--- io.netty:netty-transport:4.1.118.Final -> 4.1.128.Final (*)
2025-11-24T00:58:10.28933862Z #16 41.26 |    |    \--- io.projectreactor:reactor-core:3.6.6 -> 3.7.12
2025-11-24T00:58:10.28935443Z #16 41.26 |    |         \--- org.reactivestreams:reactive-streams:1.0.4
2025-11-24T00:58:10.28935676Z #16 41.26 |    \--- org.springframework.data:spring-data-redis:3.5.5
2025-11-24T00:58:10.289358891Z #16 41.26 |         +--- org.springframework.data:spring-data-keyvalue:3.5.5
2025-11-24T00:58:10.28936188Z #16 41.26 |         |    +--- org.springframework.data:spring-data-commons:3.5.5 (*)
2025-11-24T00:58:10.289364051Z #16 41.26 |         |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.289366321Z #16 41.26 |         |    +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:10.289368391Z #16 41.26 |         |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:10.289370481Z #16 41.26 |         +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:10.289372781Z #16 41.26 |         +--- org.springframework:spring-oxm:6.2.12
2025-11-24T00:58:10.289374841Z #16 41.26 |         |    +--- jakarta.xml.bind:jakarta.xml.bind-api:3.0.1 -> 4.0.4 (*)
2025-11-24T00:58:10.289376841Z #16 41.26 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.289378871Z #16 41.26 |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289380791Z #16 41.26 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.289382791Z #16 41.26 |         +--- org.springframework:spring-context-support:6.2.12
2025-11-24T00:58:10.289384751Z #16 41.26 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.289387121Z #16 41.26 |         |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.289389351Z #16 41.26 |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289391391Z #16 41.26 |         \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:10.289393431Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-cache -> 3.5.7
2025-11-24T00:58:10.289395521Z #16 41.26 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.289397512Z #16 41.26 |    \--- org.springframework:spring-context-support:6.2.12 (*)
2025-11-24T00:58:10.289399941Z #16 41.26 +--- net.coobird:thumbnailator:0.4.19
2025-11-24T00:58:10.289409322Z #16 41.26 +--- com.cloudinary:cloudinary-http5:2.3.0
2025-11-24T00:58:10.289411742Z #16 41.26 |    +--- com.cloudinary:cloudinary-core:2.3.0
2025-11-24T00:58:10.289413772Z #16 41.26 |    +--- org.apache.commons:commons-lang3:3.1 -> 3.18.0
2025-11-24T00:58:10.289415842Z #16 41.26 |    +--- org.apache.httpcomponents.client5:httpclient5:5.3.1 -> 5.5.1
2025-11-24T00:58:10.289417892Z #16 41.26 |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.3.6
2025-11-24T00:58:10.289420162Z #16 41.26 |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.3.6
2025-11-24T00:58:10.289422332Z #16 41.26 |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.3.6
2025-11-24T00:58:10.289424532Z #16 41.26 |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
2025-11-24T00:58:10.289426642Z #16 41.26 |    \--- org.apache.httpcomponents.core5:httpcore5:5.2.5 -> 5.3.6
2025-11-24T00:58:10.289428742Z #16 41.26 +--- io.github.openfeign.querydsl:querydsl-jpa:7.0
2025-11-24T00:58:10.289430872Z #16 41.26 |    \--- io.github.openfeign.querydsl:querydsl-core:7.0
2025-11-24T00:58:10.289432912Z #16 41.26 |         \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12 (*)
2025-11-24T00:58:10.289435172Z #16 41.26 +--- com.h2database:h2 -> 2.3.232
2025-11-24T00:58:10.289437222Z #16 41.26 +--- org.postgresql:postgresql -> 42.7.8
2025-11-24T00:58:10.289439502Z #16 41.26 |    \--- org.checkerframework:checker-qual:3.49.5
2025-11-24T00:58:10.289442333Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-data-jpa:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289444463Z #16 41.26 +--- org.springframework:spring-aspects:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289446593Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-security:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289448633Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-thymeleaf:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289450733Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-validation:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289453083Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-web:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289455143Z #16 41.26 +--- org.springframework.retry:spring-retry:{strictly 2.0.12} -> 2.0.12 (c)
2025-11-24T00:58:10.289457923Z #16 41.26 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6:{strictly 3.1.3.RELEASE} -> 3.1.3.RELEASE (c)
2025-11-24T00:58:10.289459953Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-data-redis:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289461963Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-cache:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289463973Z #16 41.26 +--- net.coobird:thumbnailator:{strictly 0.4.19} -> 0.4.19 (c)
2025-11-24T00:58:10.289466013Z #16 41.26 +--- com.cloudinary:cloudinary-http5:{strictly 2.3.0} -> 2.3.0 (c)
2025-11-24T00:58:10.289468043Z #16 41.26 +--- io.github.openfeign.querydsl:querydsl-jpa:{strictly 7.0} -> 7.0 (c)
2025-11-24T00:58:10.289470163Z #16 41.26 +--- com.h2database:h2:{strictly 2.3.232} -> 2.3.232 (c)
2025-11-24T00:58:10.289472233Z #16 41.26 +--- org.postgresql:postgresql:{strictly 42.7.8} -> 42.7.8 (c)
2025-11-24T00:58:10.289474313Z #16 41.26 +--- org.springframework.boot:spring-boot-starter:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289480214Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-jdbc:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289482744Z #16 41.26 +--- org.hibernate.orm:hibernate-core:{strictly 6.6.33.Final} -> 6.6.33.Final (c)
2025-11-24T00:58:10.289484824Z #16 41.26 +--- org.springframework.data:spring-data-jpa:{strictly 3.5.5} -> 3.5.5 (c)
2025-11-24T00:58:10.289486914Z #16 41.26 +--- org.springframework:spring-aop:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289492624Z #16 41.26 +--- org.springframework.security:spring-security-config:{strictly 6.5.6} -> 6.5.6 (c)
2025-11-24T00:58:10.289495014Z #16 41.26 +--- org.springframework.security:spring-security-web:{strictly 6.5.6} -> 6.5.6 (c)
2025-11-24T00:58:10.289497094Z #16 41.26 +--- org.thymeleaf:thymeleaf-spring6:{strictly 3.1.3.RELEASE} -> 3.1.3.RELEASE (c)
2025-11-24T00:58:10.289499274Z #16 41.26 +--- org.apache.tomcat.embed:tomcat-embed-el:{strictly 10.1.48} -> 10.1.48 (c)
2025-11-24T00:58:10.289501374Z #16 41.26 +--- org.hibernate.validator:hibernate-validator:{strictly 8.0.3.Final} -> 8.0.3.Final (c)
2025-11-24T00:58:10.289503534Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-json:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289505554Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-tomcat:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289507714Z #16 41.26 +--- org.springframework:spring-web:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289509824Z #16 41.26 +--- org.springframework:spring-webmvc:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289512074Z #16 41.26 +--- org.aspectj:aspectjweaver:{strictly 1.9.24} -> 1.9.24 (c)
2025-11-24T00:58:10.289514275Z #16 41.26 +--- org.slf4j:slf4j-api:{strictly 2.0.17} -> 2.0.17 (c)
2025-11-24T00:58:10.289516504Z #16 41.26 +--- io.lettuce:lettuce-core:{strictly 6.6.0.RELEASE} -> 6.6.0.RELEASE (c)
2025-11-24T00:58:10.289518815Z #16 41.26 +--- org.springframework.data:spring-data-redis:{strictly 3.5.5} -> 3.5.5 (c)
2025-11-24T00:58:10.289521025Z #16 41.26 +--- org.springframework:spring-context-support:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289523295Z #16 41.26 +--- com.cloudinary:cloudinary-core:{strictly 2.3.0} -> 2.3.0 (c)
2025-11-24T00:58:10.289525555Z #16 41.26 +--- org.apache.commons:commons-lang3:{strictly 3.18.0} -> 3.18.0 (c)
2025-11-24T00:58:10.289527825Z #16 41.26 +--- org.apache.httpcomponents.client5:httpclient5:{strictly 5.5.1} -> 5.5.1 (c)
2025-11-24T00:58:10.289530055Z #16 41.26 +--- org.apache.httpcomponents.core5:httpcore5:{strictly 5.3.6} -> 5.3.6 (c)
2025-11-24T00:58:10.289532325Z #16 41.26 +--- io.github.openfeign.querydsl:querydsl-core:{strictly 7.0} -> 7.0 (c)
2025-11-24T00:58:10.289534625Z #16 41.26 +--- org.checkerframework:checker-qual:{strictly 3.49.5} -> 3.49.5 (c)
2025-11-24T00:58:10.289541905Z #16 41.26 +--- org.springframework.boot:spring-boot:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289544485Z #16 41.26 +--- org.springframework.boot:spring-boot-autoconfigure:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289547065Z #16 41.26 +--- org.springframework.boot:spring-boot-starter-logging:{strictly 3.5.7} -> 3.5.7 (c)
2025-11-24T00:58:10.289549395Z #16 41.26 +--- jakarta.annotation:jakarta.annotation-api:{strictly 2.1.1} -> 2.1.1 (c)
2025-11-24T00:58:10.289551695Z #16 41.26 +--- org.springframework:spring-core:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289554016Z #16 41.26 +--- org.yaml:snakeyaml:{strictly 2.4} -> 2.4 (c)
2025-11-24T00:58:10.289556265Z #16 41.26 +--- com.zaxxer:HikariCP:{strictly 6.3.3} -> 6.3.3 (c)
2025-11-24T00:58:10.289558536Z #16 41.26 +--- org.springframework:spring-jdbc:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289560876Z #16 41.26 +--- jakarta.persistence:jakarta.persistence-api:{strictly 3.1.0} -> 3.1.0 (c)
2025-11-24T00:58:10.289563176Z #16 41.26 +--- jakarta.transaction:jakarta.transaction-api:{strictly 2.0.1} -> 2.0.1 (c)
2025-11-24T00:58:10.289565496Z #16 41.26 +--- org.jboss.logging:jboss-logging:{strictly 3.6.1.Final} -> 3.6.1.Final (c)
2025-11-24T00:58:10.289567926Z #16 41.26 +--- org.hibernate.common:hibernate-commons-annotations:{strictly 7.0.3.Final} -> 7.0.3.Final (c)
2025-11-24T00:58:10.289573626Z #16 41.26 +--- io.smallrye:jandex:{strictly 3.2.0} -> 3.2.0 (c)
2025-11-24T00:58:10.289576036Z #16 41.26 +--- com.fasterxml:classmate:{strictly 1.7.1} -> 1.7.1 (c)
2025-11-24T00:58:10.289578346Z #16 41.26 +--- net.bytebuddy:byte-buddy:{strictly 1.17.8} -> 1.17.8 (c)
2025-11-24T00:58:10.289580546Z #16 41.26 +--- jakarta.xml.bind:jakarta.xml.bind-api:{strictly 4.0.4} -> 4.0.4 (c)
2025-11-24T00:58:10.289582766Z #16 41.26 +--- org.glassfish.jaxb:jaxb-runtime:{strictly 4.0.6} -> 4.0.6 (c)
2025-11-24T00:58:10.289586406Z #16 41.26 +--- jakarta.inject:jakarta.inject-api:{strictly 2.0.1} -> 2.0.1 (c)
2025-11-24T00:58:10.289588746Z #16 41.26 +--- org.antlr:antlr4-runtime:{strictly 4.13.0} -> 4.13.0 (c)
2025-11-24T00:58:10.289590956Z #16 41.26 +--- org.springframework.data:spring-data-commons:{strictly 3.5.5} -> 3.5.5 (c)
2025-11-24T00:58:10.289593257Z #16 41.26 +--- org.springframework:spring-orm:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289595477Z #16 41.26 +--- org.springframework:spring-context:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289597777Z #16 41.26 +--- org.springframework:spring-tx:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289600127Z #16 41.26 +--- org.springframework:spring-beans:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289602617Z #16 41.26 +--- org.springframework.security:spring-security-core:{strictly 6.5.6} -> 6.5.6 (c)
2025-11-24T00:58:10.289604927Z #16 41.26 +--- org.springframework:spring-expression:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289607217Z #16 41.26 +--- org.thymeleaf:thymeleaf:{strictly 3.1.3.RELEASE} -> 3.1.3.RELEASE (c)
2025-11-24T00:58:10.289609417Z #16 41.26 +--- jakarta.validation:jakarta.validation-api:{strictly 3.0.2} -> 3.0.2 (c)
2025-11-24T00:58:10.289611757Z #16 41.26 +--- com.fasterxml.jackson.core:jackson-databind:{strictly 2.19.2} -> 2.19.2 (c)
2025-11-24T00:58:10.289613957Z #16 41.26 +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:{strictly 2.19.2} -> 2.19.2 (c)
2025-11-24T00:58:10.289616297Z #16 41.26 +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:{strictly 2.19.2} -> 2.19.2 (c)
2025-11-24T00:58:10.289618627Z #16 41.26 +--- com.fasterxml.jackson.module:jackson-module-parameter-names:{strictly 2.19.2} -> 2.19.2 (c)
2025-11-24T00:58:10.289620937Z #16 41.26 +--- org.apache.tomcat.embed:tomcat-embed-core:{strictly 10.1.48} -> 10.1.48 (c)
2025-11-24T00:58:10.289623227Z #16 41.26 +--- org.apache.tomcat.embed:tomcat-embed-websocket:{strictly 10.1.48} -> 10.1.48 (c)
2025-11-24T00:58:10.289625537Z #16 41.26 +--- io.micrometer:micrometer-observation:{strictly 1.15.5} -> 1.15.5 (c)
2025-11-24T00:58:10.289627927Z #16 41.26 +--- redis.clients.authentication:redis-authx-core:{strictly 0.1.1-beta2} -> 0.1.1-beta2 (c)
2025-11-24T00:58:10.289630187Z #16 41.26 +--- io.netty:netty-common:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
2025-11-24T00:58:10.289632458Z #16 41.26 +--- io.netty:netty-handler:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
2025-11-24T00:58:10.289634767Z #16 41.26 +--- io.netty:netty-transport:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
2025-11-24T00:58:10.289637047Z #16 41.26 +--- io.projectreactor:reactor-core:{strictly 3.7.12} -> 3.7.12 (c)
2025-11-24T00:58:10.289639288Z #16 41.26 +--- org.springframework.data:spring-data-keyvalue:{strictly 3.5.5} -> 3.5.5 (c)
2025-11-24T00:58:10.289641598Z #16 41.26 +--- org.springframework:spring-oxm:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289643888Z #16 41.26 +--- org.apache.httpcomponents.core5:httpcore5-h2:{strictly 5.3.6} -> 5.3.6 (c)
2025-11-24T00:58:10.289646118Z #16 41.26 +--- ch.qos.logback:logback-classic:{strictly 1.5.20} -> 1.5.20 (c)
2025-11-24T00:58:10.289648408Z #16 41.26 +--- org.apache.logging.log4j:log4j-to-slf4j:{strictly 2.24.3} -> 2.24.3 (c)
2025-11-24T00:58:10.289654048Z #16 41.26 +--- org.slf4j:jul-to-slf4j:{strictly 2.0.17} -> 2.0.17 (c)
2025-11-24T00:58:10.289656368Z #16 41.26 +--- org.springframework:spring-jcl:{strictly 6.2.12} -> 6.2.12 (c)
2025-11-24T00:58:10.289658598Z #16 41.27 +--- jakarta.activation:jakarta.activation-api:{strictly 2.1.4} -> 2.1.4 (c)
2025-11-24T00:58:10.289662178Z #16 41.27 +--- org.glassfish.jaxb:jaxb-core:{strictly 4.0.6} -> 4.0.6 (c)
2025-11-24T00:58:10.289664528Z #16 41.27 +--- org.springframework.security:spring-security-crypto:{strictly 6.5.6} -> 6.5.6 (c)
2025-11-24T00:58:10.289666748Z #16 41.27 +--- org.attoparser:attoparser:{strictly 2.0.7.RELEASE} -> 2.0.7.RELEASE (c)
2025-11-24T00:58:10.289669048Z #16 41.27 +--- org.unbescape:unbescape:{strictly 1.1.6.RELEASE} -> 1.1.6.RELEASE (c)
2025-11-24T00:58:10.289671399Z #16 41.27 +--- com.fasterxml.jackson.core:jackson-annotations:{strictly 2.19.2} -> 2.19.2 (c)
2025-11-24T00:58:10.289673748Z #16 41.27 +--- com.fasterxml.jackson.core:jackson-core:{strictly 2.19.2} -> 2.19.2 (c)
2025-11-24T00:58:10.289676008Z #16 41.27 +--- com.fasterxml.jackson:jackson-bom:{strictly 2.19.2} -> 2.19.2 (c)
2025-11-24T00:58:10.289678229Z #16 41.27 +--- io.micrometer:micrometer-commons:{strictly 1.15.5} -> 1.15.5 (c)
2025-11-24T00:58:10.289680479Z #16 41.27 +--- io.netty:netty-resolver:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
2025-11-24T00:58:10.289682789Z #16 41.27 +--- io.netty:netty-buffer:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
2025-11-24T00:58:10.289685119Z #16 41.27 +--- io.netty:netty-transport-native-unix-common:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
2025-11-24T00:58:10.289687309Z #16 41.27 +--- io.netty:netty-codec:{strictly 4.1.128.Final} -> 4.1.128.Final (c)
2025-11-24T00:58:10.289689639Z #16 41.27 +--- org.reactivestreams:reactive-streams:{strictly 1.0.4} -> 1.0.4 (c)
2025-11-24T00:58:10.289691859Z #16 41.27 +--- ch.qos.logback:logback-core:{strictly 1.5.20} -> 1.5.20 (c)
2025-11-24T00:58:10.289694219Z #16 41.27 +--- org.apache.logging.log4j:log4j-api:{strictly 2.24.3} -> 2.24.3 (c)
2025-11-24T00:58:10.289696569Z #16 41.27 +--- org.eclipse.angus:angus-activation:{strictly 2.0.3} -> 2.0.3 (c)
2025-11-24T00:58:10.289698819Z #16 41.27 +--- org.glassfish.jaxb:txw2:{strictly 4.0.6} -> 4.0.6 (c)
2025-11-24T00:58:10.289701079Z #16 41.27 \--- com.sun.istack:istack-commons-runtime:{strictly 4.1.2} -> 4.1.2 (c)
2025-11-24T00:58:10.289703339Z #16 41.27
2025-11-24T00:58:10.289705699Z #16 41.27 runtimeClasspath - Runtime classpath of source set 'main'.
2025-11-24T00:58:10.289707999Z #16 41.27 +--- org.springframework.boot:spring-boot-devtools -> 3.5.7
2025-11-24T00:58:10.289710369Z #16 41.27 |    +--- org.springframework.boot:spring-boot:3.5.7
2025-11-24T00:58:10.289712689Z #16 41.27 |    |    +--- org.springframework:spring-core:6.2.12
2025-11-24T00:58:10.289714949Z #16 41.27 |    |    |    \--- org.springframework:spring-jcl:6.2.12
2025-11-24T00:58:10.28971719Z #16 41.27 |    |    \--- org.springframework:spring-context:6.2.12
2025-11-24T00:58:10.28971945Z #16 41.27 |    |         +--- org.springframework:spring-aop:6.2.12
2025-11-24T00:58:10.28972173Z #16 41.27 |    |         |    +--- org.springframework:spring-beans:6.2.12
2025-11-24T00:58:10.289724Z #16 41.27 |    |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.28972644Z #16 41.27 |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.28972883Z #16 41.27 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.2897315Z #16 41.27 |    |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.28973384Z #16 41.27 |    |         +--- org.springframework:spring-expression:6.2.12
2025-11-24T00:58:10.28974003Z #16 41.27 |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.28974268Z #16 41.27 |    |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
2025-11-24T00:58:10.28974668Z #16 41.27 |    |              \--- io.micrometer:micrometer-commons:1.15.5
2025-11-24T00:58:10.289749161Z #16 41.27 |    \--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
2025-11-24T00:58:10.289751521Z #16 41.27 |         \--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:10.289754101Z #16 41.27 +--- org.springframework.boot:spring-boot-starter-data-jpa -> 3.5.7
2025-11-24T00:58:10.289756421Z #16 41.27 |    +--- org.springframework.boot:spring-boot-starter:3.5.7
2025-11-24T00:58:10.289764131Z #16 41.27 |    |    +--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:10.289767271Z #16 41.27 |    |    +--- org.springframework.boot:spring-boot-autoconfigure:3.5.7 (*)
2025-11-24T00:58:10.289770501Z #16 41.27 |    |    +--- org.springframework.boot:spring-boot-starter-logging:3.5.7
2025-11-24T00:58:10.289772921Z #16 41.27 |    |    |    +--- ch.qos.logback:logback-classic:1.5.20
2025-11-24T00:58:10.289775051Z #16 41.27 |    |    |    |    +--- ch.qos.logback:logback-core:1.5.20
2025-11-24T00:58:10.289777231Z #16 41.27 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:10.289779381Z #16 41.27 |    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.24.3
2025-11-24T00:58:10.289781441Z #16 41.27 |    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.24.3
2025-11-24T00:58:10.289783491Z #16 41.27 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:10.289785691Z #16 41.27 |    |    |    \--- org.slf4j:jul-to-slf4j:2.0.17
2025-11-24T00:58:10.289787731Z #16 41.27 |    |    |         \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:10.289789802Z #16 41.27 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
2025-11-24T00:58:10.289791791Z #16 41.27 |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289794162Z #16 41.27 |    |    \--- org.yaml:snakeyaml:2.4
2025-11-24T00:58:10.289796212Z #16 41.27 |    +--- org.springframework.boot:spring-boot-starter-jdbc:3.5.7
2025-11-24T00:58:10.289799022Z #16 41.27 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.289801172Z #16 41.27 |    |    +--- com.zaxxer:HikariCP:6.3.3
2025-11-24T00:58:10.289803132Z #16 41.27 |    |    |    \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:10.289805192Z #16 41.27 |    |    \--- org.springframework:spring-jdbc:6.2.12
2025-11-24T00:58:10.289807272Z #16 41.27 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.289809342Z #16 41.27 |    |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289811492Z #16 41.27 |    |         \--- org.springframework:spring-tx:6.2.12
2025-11-24T00:58:10.289813762Z #16 41.27 |    |              +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.289815752Z #16 41.27 |    |              \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289817712Z #16 41.27 |    +--- org.hibernate.orm:hibernate-core:6.6.33.Final
2025-11-24T00:58:10.289819682Z #16 41.27 |    |    +--- jakarta.persistence:jakarta.persistence-api:3.1.0
2025-11-24T00:58:10.289821822Z #16 41.27 |    |    +--- jakarta.transaction:jakarta.transaction-api:2.0.1
2025-11-24T00:58:10.289823812Z #16 41.27 |    |    +--- org.jboss.logging:jboss-logging:3.5.0.Final -> 3.6.1.Final
2025-11-24T00:58:10.289825842Z #16 41.27 |    |    +--- org.hibernate.common:hibernate-commons-annotations:7.0.3.Final
2025-11-24T00:58:10.289827943Z #16 41.27 |    |    +--- io.smallrye:jandex:3.2.0
2025-11-24T00:58:10.289833843Z #16 41.27 |    |    +--- com.fasterxml:classmate:1.5.1 -> 1.7.1
2025-11-24T00:58:10.289836173Z #16 41.27 |    |    +--- net.bytebuddy:byte-buddy:1.15.11 -> 1.17.8
2025-11-24T00:58:10.289838213Z #16 41.27 |    |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.0 -> 4.0.4
2025-11-24T00:58:10.289840243Z #16 41.27 |    |    |    \--- jakarta.activation:jakarta.activation-api:2.1.4
2025-11-24T00:58:10.289842213Z #16 41.27 |    |    +--- org.glassfish.jaxb:jaxb-runtime:4.0.2 -> 4.0.6
2025-11-24T00:58:10.289844273Z #16 41.27 |    |    |    \--- org.glassfish.jaxb:jaxb-core:4.0.6
2025-11-24T00:58:10.289846373Z #16 41.27 |    |    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.4 (*)
2025-11-24T00:58:10.289848913Z #16 41.27 |    |    |         +--- jakarta.activation:jakarta.activation-api:2.1.4
2025-11-24T00:58:10.289851023Z #16 41.27 |    |    |         +--- org.eclipse.angus:angus-activation:2.0.3
2025-11-24T00:58:10.289853013Z #16 41.27 |    |    |         |    \--- jakarta.activation:jakarta.activation-api:2.1.4
2025-11-24T00:58:10.289876724Z #16 41.27 |    |    |         +--- org.glassfish.jaxb:txw2:4.0.6
2025-11-24T00:58:10.289883914Z #16 41.27 |    |    |         \--- com.sun.istack:istack-commons-runtime:4.1.2
2025-11-24T00:58:10.289886174Z #16 41.27 |    |    +--- jakarta.inject:jakarta.inject-api:2.0.1
2025-11-24T00:58:10.289888304Z #16 41.27 |    |    \--- org.antlr:antlr4-runtime:4.13.0
2025-11-24T00:58:10.289890524Z #16 41.27 |    +--- org.springframework.data:spring-data-jpa:3.5.5
2025-11-24T00:58:10.289894974Z #16 41.27 |    |    +--- org.springframework.data:spring-data-commons:3.5.5
2025-11-24T00:58:10.289897214Z #16 41.27 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289899424Z #16 41.27 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.289901434Z #16 41.27 |    |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:10.289903564Z #16 41.27 |    |    +--- org.springframework:spring-orm:6.2.12
2025-11-24T00:58:10.289905674Z #16 41.27 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.289907734Z #16 41.27 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289909845Z #16 41.27 |    |    |    +--- org.springframework:spring-jdbc:6.2.12 (*)
2025-11-24T00:58:10.289911895Z #16 41.27 |    |    |    \--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:10.289914095Z #16 41.27 |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.289916295Z #16 41.27 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.289918365Z #16 41.27 |    |    +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:10.289922015Z #16 41.27 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.289924285Z #16 41.27 |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289926355Z #16 41.27 |    |    +--- org.antlr:antlr4-runtime:4.13.0
2025-11-24T00:58:10.289928495Z #16 41.27 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.0.0 -> 2.1.1
2025-11-24T00:58:10.289930795Z #16 41.27 |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:10.289932795Z #16 41.27 |    \--- org.springframework:spring-aspects:6.2.12
2025-11-24T00:58:10.289934985Z #16 41.27 |         \--- org.aspectj:aspectjweaver:1.9.22.1 -> 1.9.24
2025-11-24T00:58:10.289937005Z #16 41.27 +--- org.springframework.boot:spring-boot-starter-security -> 3.5.7
2025-11-24T00:58:10.289939155Z #16 41.27 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.289941545Z #16 41.27 |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.289951866Z #16 41.27 |    +--- org.springframework.security:spring-security-config:6.5.6
2025-11-24T00:58:10.289954716Z #16 41.27 |    |    +--- org.springframework.security:spring-security-core:6.5.6
2025-11-24T00:58:10.289956916Z #16 41.27 |    |    |    +--- org.springframework.security:spring-security-crypto:6.5.6
2025-11-24T00:58:10.289959046Z #16 41.27 |    |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.289961046Z #16 41.27 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.289963106Z #16 41.27 |    |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.289965156Z #16 41.27 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289967176Z #16 41.27 |    |    |    +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:10.289969466Z #16 41.27 |    |    |    \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
2025-11-24T00:58:10.289971556Z #16 41.27 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.289973686Z #16 41.27 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.289975756Z #16 41.27 |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.289977746Z #16 41.27 |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289979766Z #16 41.27 |    \--- org.springframework.security:spring-security-web:6.5.6
2025-11-24T00:58:10.289981806Z #16 41.27 |         +--- org.springframework.security:spring-security-core:6.5.6 (*)
2025-11-24T00:58:10.289983856Z #16 41.27 |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.289985847Z #16 41.27 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.289987927Z #16 41.27 |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.289989947Z #16 41.27 |         +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.289991997Z #16 41.27 |         +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:10.289999417Z #16 41.27 |         \--- org.springframework:spring-web:6.2.12
2025-11-24T00:58:10.290001817Z #16 41.27 |              +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.290003917Z #16 41.27 |              +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.290005977Z #16 41.27 |              \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
2025-11-24T00:58:10.290008027Z #16 41.27 +--- org.springframework.boot:spring-boot-starter-thymeleaf -> 3.5.7
2025-11-24T00:58:10.290010077Z #16 41.27 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.290012167Z #16 41.27 |    \--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE
2025-11-24T00:58:10.290014307Z #16 41.27 |         +--- org.thymeleaf:thymeleaf:3.1.3.RELEASE
2025-11-24T00:58:10.290016307Z #16 41.27 |         |    +--- org.attoparser:attoparser:2.0.7.RELEASE
2025-11-24T00:58:10.290018377Z #16 41.27 |         |    +--- org.unbescape:unbescape:1.1.6.RELEASE
2025-11-24T00:58:10.290020417Z #16 41.27 |         |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:10.290022428Z #16 41.27 |         \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:10.290024548Z #16 41.27 +--- org.springframework.boot:spring-boot-starter-validation -> 3.5.7
2025-11-24T00:58:10.290026628Z #16 41.27 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.290028608Z #16 41.27 |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
2025-11-24T00:58:10.290030658Z #16 41.27 |    \--- org.hibernate.validator:hibernate-validator:8.0.3.Final
2025-11-24T00:58:10.290036328Z #16 41.27 |         +--- jakarta.validation:jakarta.validation-api:3.0.2
2025-11-24T00:58:10.290038518Z #16 41.27 |         +--- org.jboss.logging:jboss-logging:3.4.3.Final -> 3.6.1.Final
2025-11-24T00:58:10.290040728Z #16 41.27 |         \--- com.fasterxml:classmate:1.5.1 -> 1.7.1
2025-11-24T00:58:10.290042948Z #16 41.27 +--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
2025-11-24T00:58:10.290046628Z #16 41.27 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.290048878Z #16 41.27 |    +--- org.springframework.boot:spring-boot-starter-json:3.5.7
2025-11-24T00:58:10.290050938Z #16 41.27 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.290052948Z #16 41.27 |    |    +--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:10.290054968Z #16 41.27 |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2
2025-11-24T00:58:10.290057028Z #16 41.27 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2
2025-11-24T00:58:10.290059258Z #16 41.27 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2
2025-11-24T00:58:10.290061638Z #16 41.27 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (c)
2025-11-24T00:58:10.290063798Z #16 41.27 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (c)
2025-11-24T00:58:10.290065978Z #16 41.27 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (c)
2025-11-24T00:58:10.290068078Z #16 41.27 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2 (c)
2025-11-24T00:58:10.290071929Z #16 41.27 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2 (c)
2025-11-24T00:58:10.290075279Z #16 41.27 |    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2 (c)
2025-11-24T00:58:10.290077359Z #16 41.27 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2
2025-11-24T00:58:10.290079389Z #16 41.27 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:10.290081429Z #16 41.27 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:10.290083509Z #16 41.27 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
2025-11-24T00:58:10.290085529Z #16 41.27 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:10.290087649Z #16 41.27 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:10.290089679Z #16 41.27 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:10.290091899Z #16 41.27 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
2025-11-24T00:58:10.290093919Z #16 41.27 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (*)
2025-11-24T00:58:10.290096019Z #16 41.27 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:10.290098069Z #16 41.27 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:10.290100099Z #16 41.27 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:10.29010217Z #16 41.27 |    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
2025-11-24T00:58:10.290104359Z #16 41.27 |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:10.290106399Z #16 41.27 |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:10.29010838Z #16 41.27 |    |         \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:10.29011389Z #16 41.27 |    +--- org.springframework.boot:spring-boot-starter-tomcat:3.5.7
2025-11-24T00:58:10.2901162Z #16 41.27 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
2025-11-24T00:58:10.29011962Z #16 41.27 |    |    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
2025-11-24T00:58:10.29012196Z #16 41.27 |    |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
2025-11-24T00:58:10.29012404Z #16 41.27 |    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.48
2025-11-24T00:58:10.29012616Z #16 41.27 |    |         \--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
2025-11-24T00:58:10.29012862Z #16 41.27 |    +--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:10.2901307Z #16 41.27 |    \--- org.springframework:spring-webmvc:6.2.12
2025-11-24T00:58:10.29013273Z #16 41.27 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.2901348Z #16 41.27 |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.29013695Z #16 41.27 |         +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.29013897Z #16 41.27 |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.290141031Z #16 41.27 |         +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:10.29014306Z #16 41.27 |         \--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:10.290145131Z #16 41.27 +--- org.springframework.retry:spring-retry -> 2.0.12
2025-11-24T00:58:10.290147331Z #16 41.27 +--- org.springframework:spring-aspects -> 6.2.12 (*)
2025-11-24T00:58:10.290149381Z #16 41.27 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 -> 3.1.3.RELEASE
2025-11-24T00:58:10.290151421Z #16 41.27 |    +--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE (*)
2025-11-24T00:58:10.290153481Z #16 41.27 |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:10.290155451Z #16 41.27 +--- org.springframework.boot:spring-boot-starter-data-redis -> 3.5.7
2025-11-24T00:58:10.290157521Z #16 41.27 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.290159501Z #16 41.27 |    +--- io.lettuce:lettuce-core:6.6.0.RELEASE
2025-11-24T00:58:10.290161501Z #16 41.27 |    |    +--- redis.clients.authentication:redis-authx-core:0.1.1-beta2
2025-11-24T00:58:10.290163671Z #16 41.27 |    |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
2025-11-24T00:58:10.290169501Z #16 41.27 |    |    +--- io.netty:netty-common:4.1.118.Final -> 4.1.128.Final
2025-11-24T00:58:10.290172071Z #16 41.27 |    |    +--- io.netty:netty-handler:4.1.118.Final -> 4.1.128.Final
2025-11-24T00:58:10.290174191Z #16 41.27 |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.290176171Z #16 41.27 |    |    |    +--- io.netty:netty-resolver:4.1.128.Final
2025-11-24T00:58:10.290178361Z #16 41.27 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.290180581Z #16 41.27 |    |    |    +--- io.netty:netty-buffer:4.1.128.Final
2025-11-24T00:58:10.290182541Z #16 41.27 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.290184581Z #16 41.27 |    |    |    +--- io.netty:netty-transport:4.1.128.Final
2025-11-24T00:58:10.290186612Z #16 41.27 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.290188612Z #16 41.27 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:10.290190732Z #16 41.27 |    |    |    |    \--- io.netty:netty-resolver:4.1.128.Final (*)
2025-11-24T00:58:10.290192712Z #16 41.27 |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.128.Final
2025-11-24T00:58:10.290194742Z #16 41.27 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.290200192Z #16 41.27 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:10.290202452Z #16 41.27 |    |    |    |    \--- io.netty:netty-transport:4.1.128.Final (*)
2025-11-24T00:58:10.290204572Z #16 41.27 |    |    |    \--- io.netty:netty-codec:4.1.128.Final
2025-11-24T00:58:10.290206722Z #16 41.27 |    |    |         +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:10.290208892Z #16 41.27 |    |    |         +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:10.290211022Z #16 41.27 |    |    |         \--- io.netty:netty-transport:4.1.128.Final (*)
2025-11-24T00:58:10.290213152Z #16 41.27 |    |    +--- io.netty:netty-transport:4.1.118.Final -> 4.1.128.Final (*)
2025-11-24T00:58:10.290215142Z #16 41.27 |    |    \--- io.projectreactor:reactor-core:3.6.6 -> 3.7.12
2025-11-24T00:58:10.290217172Z #16 41.27 |    |         \--- org.reactivestreams:reactive-streams:1.0.4
2025-11-24T00:58:10.290219182Z #16 41.27 |    \--- org.springframework.data:spring-data-redis:3.5.5
2025-11-24T00:58:10.290221262Z #16 41.27 |         +--- org.springframework.data:spring-data-keyvalue:3.5.5
2025-11-24T00:58:10.290223293Z #16 41.27 |         |    +--- org.springframework.data:spring-data-commons:3.5.5 (*)
2025-11-24T00:58:10.290225442Z #16 41.27 |         |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.290227423Z #16 41.27 |         |    +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:10.290229423Z #16 41.27 |         |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:10.290231443Z #16 41.27 |         +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:10.290233473Z #16 41.27 |         +--- org.springframework:spring-oxm:6.2.12
2025-11-24T00:58:10.290235493Z #16 41.27 |         |    +--- jakarta.xml.bind:jakarta.xml.bind-api:3.0.1 -> 4.0.4 (*)
2025-11-24T00:58:10.290237513Z #16 41.27 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.290239583Z #16 41.27 |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.290241633Z #16 41.27 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:10.290243703Z #16 41.27 |         +--- org.springframework:spring-context-support:6.2.12
2025-11-24T00:58:10.290245723Z #16 41.27 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:10.290247833Z #16 41.27 |         |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:10.290249843Z #16 41.27 |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:10.290251943Z #16 41.27 |         \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:10.290254003Z #16 41.27 +--- org.springframework.boot:spring-boot-starter-cache -> 3.5.7
2025-11-24T00:58:10.290256033Z #16 41.27 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:10.290258123Z #16 41.27 |    \--- org.springframework:spring-context-support:6.2.12 (*)
2025-11-24T00:58:10.290260703Z #16 41.27 +--- net.coobird:thumbnailator:0.4.19
2025-11-24T00:58:10.290262834Z #16 41.27 +--- com.cloudinary:cloudinary-http5:2.3.0
2025-11-24T00:58:10.290264974Z #16 41.27 |    +--- com.cloudinary:cloudinary-core:2.3.0
2025-11-24T00:58:10.290267084Z #16 41.27 |    +--- org.apache.commons:commons-lang3:3.1 -> 3.18.0
2025-11-24T00:58:10.290269114Z #16 41.27 |    +--- org.apache.httpcomponents.client5:httpclient5:5.3.1 -> 5.5.1
2025-11-24T00:58:10.290271104Z #16 41.27 |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.3.6
2025-11-24T00:58:10.290273174Z #16 41.27 |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.3.6
2025-11-24T00:58:10.290275214Z #16 41.27 |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.3.6
2025-11-24T00:58:10.290281394Z #16 41.27 |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
2025-11-24T00:58:10.290283694Z #16 41.27 |    \--- org.apache.httpcomponents.core5:httpcore5:5.2.5 -> 5.3.6
2025-11-24T00:58:10.290285754Z #16 41.27 +--- io.github.openfeign.querydsl:querydsl-jpa:7.0
2025-11-24T00:58:10.290287794Z #16 41.27 |    \--- io.github.openfeign.querydsl:querydsl-core:7.0
2025-11-24T00:58:10.290289824Z #16 41.27 |         \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12 (*)
2025-11-24T00:58:10.290291864Z #16 41.27 +--- com.h2database:h2 -> 2.3.232
2025-11-24T00:58:10.290293924Z #16 41.27 \--- org.postgresql:postgresql -> 42.7.8
2025-11-24T00:58:10.290297044Z #16 41.27      \--- org.checkerframework:checker-qual:3.49.5
2025-11-24T00:58:10.290299335Z #16 41.27
2025-11-24T00:58:10.290302255Z #16 41.27 runtimeElements - Runtime elements for the 'main' feature. (n)
2025-11-24T00:58:10.290304915Z #16 41.27 No dependencies
2025-11-24T00:58:10.290306985Z #16 41.27
2025-11-24T00:58:10.290309115Z #16 41.27 runtimeOnly - Runtime-only dependencies for the 'main' feature. (n)
2025-11-24T00:58:10.290311315Z #16 41.27 +--- com.h2database:h2 (n)
2025-11-24T00:58:10.290313435Z #16 41.27 \--- org.postgresql:postgresql (n)
2025-11-24T00:58:10.290315575Z #16 41.27
2025-11-24T00:58:10.290318255Z #16 41.27 testAndDevelopmentOnly - Configuration for test and development-only dependencies such as Spring Boot's DevTools.
2025-11-24T00:58:10.290320305Z #16 41.27 No dependencies
2025-11-24T00:58:10.290322375Z #16 41.27
2025-11-24T00:58:10.290324525Z #16 41.27 testAnnotationProcessor - Annotation processors and their dependencies for source set 'test'.
2025-11-24T00:58:10.290326605Z #16 41.27 No dependencies
2025-11-24T00:58:10.290328675Z #16 41.27
2025-11-24T00:58:10.290330805Z #16 41.27 testCompileClasspath - Compile classpath for source set 'test'.
2025-11-24T00:58:11.828098976Z #16 42.96 +--- org.springframework.boot:spring-boot-starter-data-jpa -> 3.5.7
2025-11-24T00:58:11.828125577Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter:3.5.7
2025-11-24T00:58:11.828130907Z #16 42.96 |    |    +--- org.springframework.boot:spring-boot:3.5.7
2025-11-24T00:58:11.928025701Z #16 42.96 |    |    |    +--- org.springframework:spring-core:6.2.12
2025-11-24T00:58:11.930387101Z #16 42.96 |    |    |    |    \--- org.springframework:spring-jcl:6.2.12
2025-11-24T00:58:11.930397591Z #16 42.96 |    |    |    \--- org.springframework:spring-context:6.2.12
2025-11-24T00:58:11.930400972Z #16 42.96 |    |    |         +--- org.springframework:spring-aop:6.2.12
2025-11-24T00:58:11.930403932Z #16 42.96 |    |    |         |    +--- org.springframework:spring-beans:6.2.12
2025-11-24T00:58:11.930407052Z #16 42.96 |    |    |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930409922Z #16 42.96 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930412782Z #16 42.96 |    |    |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930415682Z #16 42.96 |    |    |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930418452Z #16 42.96 |    |    |         +--- org.springframework:spring-expression:6.2.12
2025-11-24T00:58:11.930421292Z #16 42.96 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930424162Z #16 42.96 |    |    |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
2025-11-24T00:58:11.930427042Z #16 42.96 |    |    |              \--- io.micrometer:micrometer-commons:1.15.5
2025-11-24T00:58:11.930429832Z #16 42.96 |    |    +--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
2025-11-24T00:58:11.930442073Z #16 42.96 |    |    |    \--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:11.930446153Z #16 42.96 |    |    +--- org.springframework.boot:spring-boot-starter-logging:3.5.7
2025-11-24T00:58:11.930448083Z #16 42.96 |    |    |    +--- ch.qos.logback:logback-classic:1.5.20
2025-11-24T00:58:11.930449783Z #16 42.96 |    |    |    |    +--- ch.qos.logback:logback-core:1.5.20
2025-11-24T00:58:11.930452303Z #16 42.96 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:11.930454023Z #16 42.96 |    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.24.3
2025-11-24T00:58:11.930455693Z #16 42.96 |    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.24.3
2025-11-24T00:58:11.930457363Z #16 42.96 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:11.930459073Z #16 42.96 |    |    |    \--- org.slf4j:jul-to-slf4j:2.0.17
2025-11-24T00:58:11.930460813Z #16 42.96 |    |    |         \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:11.930462633Z #16 42.96 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
2025-11-24T00:58:11.930464343Z #16 42.96 |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930467053Z #16 42.96 |    |    \--- org.yaml:snakeyaml:2.4
2025-11-24T00:58:11.930468803Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter-jdbc:3.5.7
2025-11-24T00:58:11.930470513Z #16 42.96 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.930493554Z #16 42.96 |    |    +--- com.zaxxer:HikariCP:6.3.3
2025-11-24T00:58:11.930495944Z #16 42.96 |    |    |    \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:11.930497744Z #16 42.96 |    |    \--- org.springframework:spring-jdbc:6.2.12
2025-11-24T00:58:11.930499604Z #16 42.96 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930501304Z #16 42.96 |    |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930503314Z #16 42.96 |    |         \--- org.springframework:spring-tx:6.2.12
2025-11-24T00:58:11.930505024Z #16 42.96 |    |              +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930506714Z #16 42.96 |    |              \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930508474Z #16 42.96 |    +--- org.hibernate.orm:hibernate-core:6.6.33.Final
2025-11-24T00:58:11.930510154Z #16 42.96 |    |    +--- jakarta.persistence:jakarta.persistence-api:3.1.0
2025-11-24T00:58:11.930511924Z #16 42.96 |    |    \--- jakarta.transaction:jakarta.transaction-api:2.0.1
2025-11-24T00:58:11.930513584Z #16 42.96 |    +--- org.springframework.data:spring-data-jpa:3.5.5
2025-11-24T00:58:11.930515284Z #16 42.96 |    |    +--- org.springframework.data:spring-data-commons:3.5.5
2025-11-24T00:58:11.930516954Z #16 42.96 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930518624Z #16 42.96 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930520284Z #16 42.96 |    |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:11.930521984Z #16 42.96 |    |    +--- org.springframework:spring-orm:6.2.12
2025-11-24T00:58:11.930528485Z #16 42.96 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930530355Z #16 42.96 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930532065Z #16 42.96 |    |    |    +--- org.springframework:spring-jdbc:6.2.12 (*)
2025-11-24T00:58:11.930533765Z #16 42.96 |    |    |    \--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:11.930535435Z #16 42.96 |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.930540855Z #16 42.96 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.930542745Z #16 42.96 |    |    +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:11.930544515Z #16 42.96 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930546185Z #16 42.96 |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930547875Z #16 42.96 |    |    +--- org.antlr:antlr4-runtime:4.13.0
2025-11-24T00:58:11.930549765Z #16 42.96 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.0.0 -> 2.1.1
2025-11-24T00:58:11.930551645Z #16 42.96 |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:11.930553385Z #16 42.96 |    \--- org.springframework:spring-aspects:6.2.12
2025-11-24T00:58:11.930555065Z #16 42.96 |         \--- org.aspectj:aspectjweaver:1.9.22.1 -> 1.9.24
2025-11-24T00:58:11.930556735Z #16 42.96 +--- org.springframework.boot:spring-boot-starter-security -> 3.5.7
2025-11-24T00:58:11.930558396Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.930560105Z #16 42.96 |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.930561785Z #16 42.96 |    +--- org.springframework.security:spring-security-config:6.5.6
2025-11-24T00:58:11.930563496Z #16 42.96 |    |    +--- org.springframework.security:spring-security-core:6.5.6
2025-11-24T00:58:11.930565216Z #16 42.96 |    |    |    +--- org.springframework.security:spring-security-crypto:6.5.6
2025-11-24T00:58:11.930566866Z #16 42.96 |    |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.930568536Z #16 42.96 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930570246Z #16 42.96 |    |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.930571976Z #16 42.96 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930573656Z #16 42.96 |    |    |    +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:11.930575346Z #16 42.96 |    |    |    \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
2025-11-24T00:58:11.930577056Z #16 42.96 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.930578716Z #16 42.96 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930580416Z #16 42.96 |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.930582066Z #16 42.96 |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930583756Z #16 42.96 |    \--- org.springframework.security:spring-security-web:6.5.6
2025-11-24T00:58:11.930585426Z #16 42.96 |         +--- org.springframework.security:spring-security-core:6.5.6 (*)
2025-11-24T00:58:11.930587096Z #16 42.96 |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930588766Z #16 42.96 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.930590466Z #16 42.96 |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930593556Z #16 42.96 |         +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.930595346Z #16 42.96 |         +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:11.930597056Z #16 42.96 |         \--- org.springframework:spring-web:6.2.12
2025-11-24T00:58:11.930598757Z #16 42.96 |              +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930600417Z #16 42.96 |              +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930602106Z #16 42.96 |              \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
2025-11-24T00:58:11.930606417Z #16 42.96 +--- org.springframework.boot:spring-boot-starter-thymeleaf -> 3.5.7
2025-11-24T00:58:11.930608207Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.930609877Z #16 42.96 |    \--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE
2025-11-24T00:58:11.930611577Z #16 42.96 |         +--- org.thymeleaf:thymeleaf:3.1.3.RELEASE
2025-11-24T00:58:11.930613247Z #16 42.96 |         |    +--- org.attoparser:attoparser:2.0.7.RELEASE
2025-11-24T00:58:11.930614937Z #16 42.96 |         |    +--- org.unbescape:unbescape:1.1.6.RELEASE
2025-11-24T00:58:11.930616597Z #16 42.96 |         |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:11.930618297Z #16 42.96 |         \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:11.930619967Z #16 42.96 +--- org.springframework.boot:spring-boot-starter-validation -> 3.5.7
2025-11-24T00:58:11.930621677Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.930623327Z #16 42.96 |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
2025-11-24T00:58:11.930625017Z #16 42.96 |    \--- org.hibernate.validator:hibernate-validator:8.0.3.Final
2025-11-24T00:58:11.930626687Z #16 42.96 |         +--- jakarta.validation:jakarta.validation-api:3.0.2
2025-11-24T00:58:11.930628347Z #16 42.96 |         +--- org.jboss.logging:jboss-logging:3.4.3.Final -> 3.6.1.Final
2025-11-24T00:58:11.930630027Z #16 42.96 |         \--- com.fasterxml:classmate:1.5.1 -> 1.7.1
2025-11-24T00:58:11.930636667Z #16 42.96 +--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
2025-11-24T00:58:11.930638578Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.930640267Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter-json:3.5.7
2025-11-24T00:58:11.930641918Z #16 42.96 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.930643558Z #16 42.96 |    |    +--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:11.930645208Z #16 42.96 |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2
2025-11-24T00:58:11.930646868Z #16 42.96 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2
2025-11-24T00:58:11.930648548Z #16 42.96 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2
2025-11-24T00:58:11.930652828Z #16 42.96 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (c)
2025-11-24T00:58:11.930654578Z #16 42.96 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (c)
2025-11-24T00:58:11.930656388Z #16 42.96 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (c)
2025-11-24T00:58:11.930658198Z #16 42.96 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2 (c)
2025-11-24T00:58:11.930659938Z #16 42.96 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2 (c)
2025-11-24T00:58:11.930661608Z #16 42.96 |    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2 (c)
2025-11-24T00:58:11.930663278Z #16 42.96 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2
2025-11-24T00:58:11.930665028Z #16 42.96 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:11.930666728Z #16 42.96 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:11.930668538Z #16 42.96 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
2025-11-24T00:58:11.930670238Z #16 42.96 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:11.930671908Z #16 42.96 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:11.930676288Z #16 42.96 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:11.930678019Z #16 42.96 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
2025-11-24T00:58:11.930679728Z #16 42.96 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (*)
2025-11-24T00:58:11.930681448Z #16 42.96 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:11.930683168Z #16 42.96 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:11.930684889Z #16 42.96 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:11.930686569Z #16 42.96 |    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
2025-11-24T00:58:11.930688289Z #16 42.96 |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:11.930689959Z #16 42.96 |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:11.930691639Z #16 42.96 |    |         \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:11.930693369Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter-tomcat:3.5.7
2025-11-24T00:58:11.930695059Z #16 42.96 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
2025-11-24T00:58:11.930696739Z #16 42.96 |    |    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
2025-11-24T00:58:11.930698449Z #16 42.96 |    |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
2025-11-24T00:58:11.930700129Z #16 42.96 |    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.48
2025-11-24T00:58:11.930701789Z #16 42.96 |    |         \--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
2025-11-24T00:58:11.930703509Z #16 42.96 |    +--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:11.930705179Z #16 42.96 |    \--- org.springframework:spring-webmvc:6.2.12
2025-11-24T00:58:11.930706869Z #16 42.96 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.930708549Z #16 42.96 |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930710229Z #16 42.96 |         +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.930711939Z #16 42.96 |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930713619Z #16 42.96 |         +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:11.93071528Z #16 42.96 |         \--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:11.930717009Z #16 42.96 +--- org.springframework.retry:spring-retry -> 2.0.12
2025-11-24T00:58:11.9307188Z #16 42.96 +--- org.springframework:spring-aspects -> 6.2.12 (*)
2025-11-24T00:58:11.930720469Z #16 42.96 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 -> 3.1.3.RELEASE
2025-11-24T00:58:11.930722129Z #16 42.96 |    +--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE (*)
2025-11-24T00:58:11.93072384Z #16 42.96 |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:11.93072553Z #16 42.96 +--- org.springframework.boot:spring-boot-starter-data-redis -> 3.5.7
2025-11-24T00:58:11.9307272Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.93072893Z #16 42.96 |    +--- io.lettuce:lettuce-core:6.6.0.RELEASE
2025-11-24T00:58:11.93073472Z #16 42.96 |    |    +--- redis.clients.authentication:redis-authx-core:0.1.1-beta2
2025-11-24T00:58:11.93073656Z #16 42.96 |    |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
2025-11-24T00:58:11.93073825Z #16 42.96 |    |    +--- io.netty:netty-common:4.1.118.Final -> 4.1.128.Final
2025-11-24T00:58:11.93073993Z #16 42.96 |    |    +--- io.netty:netty-handler:4.1.118.Final -> 4.1.128.Final
2025-11-24T00:58:11.93074424Z #16 42.96 |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:11.930746Z #16 42.96 |    |    |    +--- io.netty:netty-resolver:4.1.128.Final
2025-11-24T00:58:11.93074769Z #16 42.96 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:11.93074941Z #16 42.96 |    |    |    +--- io.netty:netty-buffer:4.1.128.Final
2025-11-24T00:58:11.93075113Z #16 42.96 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:11.93075281Z #16 42.96 |    |    |    +--- io.netty:netty-transport:4.1.128.Final
2025-11-24T00:58:11.930754481Z #16 42.96 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:11.93075615Z #16 42.96 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:11.93075787Z #16 42.96 |    |    |    |    \--- io.netty:netty-resolver:4.1.128.Final (*)
2025-11-24T00:58:11.93075953Z #16 42.96 |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.128.Final
2025-11-24T00:58:11.930761201Z #16 42.96 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:11.930762901Z #16 42.96 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:11.930764571Z #16 42.96 |    |    |    |    \--- io.netty:netty-transport:4.1.128.Final (*)
2025-11-24T00:58:11.930766301Z #16 42.96 |    |    |    \--- io.netty:netty-codec:4.1.128.Final
2025-11-24T00:58:11.930767981Z #16 42.96 |    |    |         +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:11.930769631Z #16 42.96 |    |    |         +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:11.930771321Z #16 42.96 |    |    |         \--- io.netty:netty-transport:4.1.128.Final (*)
2025-11-24T00:58:11.930772991Z #16 42.96 |    |    +--- io.netty:netty-transport:4.1.118.Final -> 4.1.128.Final (*)
2025-11-24T00:58:11.930774681Z #16 42.96 |    |    \--- io.projectreactor:reactor-core:3.6.6 -> 3.7.12
2025-11-24T00:58:11.930776361Z #16 42.96 |    |         \--- org.reactivestreams:reactive-streams:1.0.4
2025-11-24T00:58:11.930778031Z #16 42.96 |    \--- org.springframework.data:spring-data-redis:3.5.5
2025-11-24T00:58:11.930779671Z #16 42.96 |         +--- org.springframework.data:spring-data-keyvalue:3.5.5
2025-11-24T00:58:11.930781361Z #16 42.96 |         |    +--- org.springframework.data:spring-data-commons:3.5.5 (*)
2025-11-24T00:58:11.930783031Z #16 42.96 |         |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.930784721Z #16 42.96 |         |    +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:11.930786371Z #16 42.96 |         |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:11.930788051Z #16 42.96 |         +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:11.930789701Z #16 42.96 |         +--- org.springframework:spring-oxm:6.2.12
2025-11-24T00:58:11.930791401Z #16 42.96 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930793051Z #16 42.96 |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930794722Z #16 42.96 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.930796371Z #16 42.96 |         +--- org.springframework:spring-context-support:6.2.12
2025-11-24T00:58:11.930798082Z #16 42.96 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930799731Z #16 42.96 |         |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.930801412Z #16 42.96 |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930803082Z #16 42.96 |         \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:11.930807512Z #16 42.96 +--- org.springframework.boot:spring-boot-starter-cache -> 3.5.7
2025-11-24T00:58:11.930809222Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.930810892Z #16 42.96 |    \--- org.springframework:spring-context-support:6.2.12 (*)
2025-11-24T00:58:11.930812662Z #16 42.96 +--- net.coobird:thumbnailator:0.4.19
2025-11-24T00:58:11.930814372Z #16 42.96 +--- com.cloudinary:cloudinary-http5:2.3.0
2025-11-24T00:58:11.930816072Z #16 42.96 |    +--- com.cloudinary:cloudinary-core:2.3.0
2025-11-24T00:58:11.930818342Z #16 42.96 |    +--- org.apache.commons:commons-lang3:3.1 -> 3.18.0
2025-11-24T00:58:11.930820032Z #16 42.96 |    +--- org.apache.httpcomponents.client5:httpclient5:5.3.1 -> 5.5.1
2025-11-24T00:58:11.930821692Z #16 42.96 |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.3.6
2025-11-24T00:58:11.930823352Z #16 42.96 |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.3.6
2025-11-24T00:58:11.930825012Z #16 42.96 |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.3.6
2025-11-24T00:58:11.930826682Z #16 42.96 |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
2025-11-24T00:58:11.930828372Z #16 42.96 |    \--- org.apache.httpcomponents.core5:httpcore5:5.2.5 -> 5.3.6
2025-11-24T00:58:11.930830042Z #16 42.96 +--- io.github.openfeign.querydsl:querydsl-jpa:7.0
2025-11-24T00:58:11.930831732Z #16 42.96 |    \--- io.github.openfeign.querydsl:querydsl-core:7.0
2025-11-24T00:58:11.930833372Z #16 42.96 |         \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12 (*)
2025-11-24T00:58:11.930835032Z #16 42.96 +--- org.springframework.boot:spring-boot-starter-test -> 3.5.7
2025-11-24T00:58:11.930843363Z #16 42.96 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.930845343Z #16 42.96 |    +--- org.springframework.boot:spring-boot-test:3.5.7
2025-11-24T00:58:11.930847103Z #16 42.96 |    |    +--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:11.930848793Z #16 42.96 |    |    \--- org.springframework:spring-test:6.2.12
2025-11-24T00:58:11.930850493Z #16 42.96 |    |         \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930852393Z #16 42.96 |    +--- org.springframework.boot:spring-boot-test-autoconfigure:3.5.7
2025-11-24T00:58:11.930868763Z #16 42.96 |    |    +--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:11.930873953Z #16 42.96 |    |    +--- org.springframework.boot:spring-boot-test:3.5.7 (*)
2025-11-24T00:58:11.930875773Z #16 42.96 |    |    \--- org.springframework.boot:spring-boot-autoconfigure:3.5.7 (*)
2025-11-24T00:58:11.930877464Z #16 42.96 |    +--- com.jayway.jsonpath:json-path:2.9.0
2025-11-24T00:58:11.930879244Z #16 42.96 |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.4
2025-11-24T00:58:11.930880954Z #16 42.96 |    |    \--- jakarta.activation:jakarta.activation-api:2.1.4
2025-11-24T00:58:11.930882644Z #16 42.96 |    +--- net.minidev:json-smart:2.5.2
2025-11-24T00:58:11.930884314Z #16 42.96 |    |    \--- net.minidev:accessors-smart:2.5.2
2025-11-24T00:58:11.930886064Z #16 42.96 |    |         \--- org.ow2.asm:asm:9.7.1
2025-11-24T00:58:11.930887734Z #16 42.96 |    +--- org.assertj:assertj-core:3.27.6
2025-11-24T00:58:11.930889434Z #16 42.96 |    |    \--- net.bytebuddy:byte-buddy:1.17.7 -> 1.17.8
2025-11-24T00:58:11.930891124Z #16 42.96 |    +--- org.awaitility:awaitility:4.2.2
2025-11-24T00:58:11.930892824Z #16 42.96 |    |    \--- org.hamcrest:hamcrest:2.1 -> 3.0
2025-11-24T00:58:11.930894574Z #16 42.96 |    +--- org.hamcrest:hamcrest:3.0
2025-11-24T00:58:11.930896634Z #16 42.96 |    +--- org.junit.jupiter:junit-jupiter:5.12.2
2025-11-24T00:58:11.930898324Z #16 42.96 |    |    +--- org.junit:junit-bom:5.12.2
2025-11-24T00:58:11.930903774Z #16 42.96 |    |    |    +--- org.junit.jupiter:junit-jupiter:5.12.2 (c)
2025-11-24T00:58:11.930905514Z #16 42.96 |    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.12.2 (c)
2025-11-24T00:58:11.930907264Z #16 42.96 |    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.12.2 (c)
2025-11-24T00:58:11.930909004Z #16 42.96 |    |    |    \--- org.junit.platform:junit-platform-commons:1.12.2 (c)
2025-11-24T00:58:11.930910794Z #16 42.96 |    |    +--- org.junit.jupiter:junit-jupiter-api:5.12.2
2025-11-24T00:58:11.930912534Z #16 42.96 |    |    |    +--- org.junit:junit-bom:5.12.2 (*)
2025-11-24T00:58:11.930914214Z #16 42.96 |    |    |    +--- org.opentest4j:opentest4j:1.3.0
2025-11-24T00:58:11.930915905Z #16 42.96 |    |    |    +--- org.junit.platform:junit-platform-commons:1.12.2
2025-11-24T00:58:11.930917634Z #16 42.96 |    |    |    |    +--- org.junit:junit-bom:5.12.2 (*)
2025-11-24T00:58:11.930919325Z #16 42.96 |    |    |    |    \--- org.apiguardian:apiguardian-api:1.1.2
2025-11-24T00:58:11.930920985Z #16 42.96 |    |    |    \--- org.apiguardian:apiguardian-api:1.1.2
2025-11-24T00:58:11.930922655Z #16 42.96 |    |    \--- org.junit.jupiter:junit-jupiter-params:5.12.2
2025-11-24T00:58:11.930924435Z #16 42.96 |    |         +--- org.junit:junit-bom:5.12.2 (*)
2025-11-24T00:58:11.930926115Z #16 42.96 |    |         +--- org.junit.jupiter:junit-jupiter-api:5.12.2 (*)
2025-11-24T00:58:11.930927775Z #16 42.96 |    |         \--- org.apiguardian:apiguardian-api:1.1.2
2025-11-24T00:58:11.930929475Z #16 42.96 |    +--- org.mockito:mockito-core:5.17.0
2025-11-24T00:58:11.930931135Z #16 42.96 |    |    +--- net.bytebuddy:byte-buddy:1.15.11 -> 1.17.8
2025-11-24T00:58:11.930932785Z #16 42.96 |    |    \--- net.bytebuddy:byte-buddy-agent:1.15.11 -> 1.17.8
2025-11-24T00:58:11.930934475Z #16 42.96 |    +--- org.mockito:mockito-junit-jupiter:5.17.0
2025-11-24T00:58:11.930936165Z #16 42.96 |    |    \--- org.mockito:mockito-core:5.17.0 (*)
2025-11-24T00:58:11.930937885Z #16 42.96 |    +--- org.skyscreamer:jsonassert:1.5.3
2025-11-24T00:58:11.930939605Z #16 42.96 |    |    \--- com.vaadin.external.google:android-json:0.0.20131108.vaadin1
2025-11-24T00:58:11.930941265Z #16 42.96 |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930942935Z #16 42.96 |    +--- org.springframework:spring-test:6.2.12 (*)
2025-11-24T00:58:11.930944615Z #16 42.96 |    \--- org.xmlunit:xmlunit-core:2.10.4
2025-11-24T00:58:11.930946295Z #16 42.96 \--- org.springframework.security:spring-security-test -> 6.5.6
2025-11-24T00:58:11.930947995Z #16 42.96      +--- org.springframework.security:spring-security-core:6.5.6 (*)
2025-11-24T00:58:11.930949675Z #16 42.96      +--- org.springframework.security:spring-security-web:6.5.6 (*)
2025-11-24T00:58:11.930951386Z #16 42.96      +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930953075Z #16 42.96      \--- org.springframework:spring-test:6.2.12 (*)
2025-11-24T00:58:11.930954806Z #16 42.96
2025-11-24T00:58:11.930956566Z #16 42.96 testCompileOnly - Compile only dependencies for source set 'test'. (n)
2025-11-24T00:58:11.930958816Z #16 42.96 No dependencies
2025-11-24T00:58:11.930960516Z #16 42.96
2025-11-24T00:58:11.930962196Z #16 42.96 testImplementation - Implementation only dependencies for source set 'test'. (n)
2025-11-24T00:58:11.930963896Z #16 42.96 +--- org.springframework.boot:spring-boot-starter-test (n)
2025-11-24T00:58:11.930965596Z #16 42.96 \--- org.springframework.security:spring-security-test (n)
2025-11-24T00:58:11.930967296Z #16 42.96
2025-11-24T00:58:11.930968936Z #16 42.96 testRuntimeClasspath - Runtime classpath of source set 'test'.
2025-11-24T00:58:11.930973056Z #16 43.05 +--- org.springframework.boot:spring-boot-starter-data-jpa -> 3.5.7
2025-11-24T00:58:11.930974786Z #16 43.05 |    +--- org.springframework.boot:spring-boot-starter:3.5.7
2025-11-24T00:58:11.930981916Z #16 43.05 |    |    +--- org.springframework.boot:spring-boot:3.5.7
2025-11-24T00:58:11.930983766Z #16 43.05 |    |    |    +--- org.springframework:spring-core:6.2.12
2025-11-24T00:58:11.930985476Z #16 43.05 |    |    |    |    \--- org.springframework:spring-jcl:6.2.12
2025-11-24T00:58:11.930987176Z #16 43.05 |    |    |    \--- org.springframework:spring-context:6.2.12
2025-11-24T00:58:11.930988856Z #16 43.05 |    |    |         +--- org.springframework:spring-aop:6.2.12
2025-11-24T00:58:11.930990547Z #16 43.05 |    |    |         |    +--- org.springframework:spring-beans:6.2.12
2025-11-24T00:58:11.930992336Z #16 43.05 |    |    |         |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930994076Z #16 43.05 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930995747Z #16 43.05 |    |    |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.930997427Z #16 43.05 |    |    |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.930999107Z #16 43.05 |    |    |         +--- org.springframework:spring-expression:6.2.12
2025-11-24T00:58:11.931000817Z #16 43.05 |    |    |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.931002477Z #16 43.05 |    |    |         \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5
2025-11-24T00:58:11.931004167Z #16 43.05 |    |    |              \--- io.micrometer:micrometer-commons:1.15.5
2025-11-24T00:58:11.931005837Z #16 43.05 |    |    +--- org.springframework.boot:spring-boot-autoconfigure:3.5.7
2025-11-24T00:58:11.931007507Z #16 43.05 |    |    |    \--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:11.931009357Z #16 43.05 |    |    +--- org.springframework.boot:spring-boot-starter-logging:3.5.7
2025-11-24T00:58:11.931011087Z #16 43.05 |    |    |    +--- ch.qos.logback:logback-classic:1.5.20
2025-11-24T00:58:11.931012997Z #16 43.05 |    |    |    |    +--- ch.qos.logback:logback-core:1.5.20
2025-11-24T00:58:11.931014677Z #16 43.05 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:11.931016417Z #16 43.05 |    |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.24.3
2025-11-24T00:58:11.931018097Z #16 43.05 |    |    |    |    +--- org.apache.logging.log4j:log4j-api:2.24.3
2025-11-24T00:58:11.931019777Z #16 43.05 |    |    |    |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:11.931021467Z #16 43.05 |    |    |    \--- org.slf4j:jul-to-slf4j:2.0.17
2025-11-24T00:58:11.931023157Z #16 43.05 |    |    |         \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:11.931024837Z #16 43.05 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
2025-11-24T00:58:11.931026527Z #16 43.05 |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.931028277Z #16 43.05 |    |    \--- org.yaml:snakeyaml:2.4
2025-11-24T00:58:11.931031168Z #16 43.05 |    +--- org.springframework.boot:spring-boot-starter-jdbc:3.5.7
2025-11-24T00:58:11.931032977Z #16 43.05 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.931034668Z #16 43.05 |    |    +--- com.zaxxer:HikariCP:6.3.3
2025-11-24T00:58:11.931036397Z #16 43.05 |    |    |    \--- org.slf4j:slf4j-api:2.0.17
2025-11-24T00:58:11.931038078Z #16 43.05 |    |    \--- org.springframework:spring-jdbc:6.2.12
2025-11-24T00:58:11.931039758Z #16 43.05 |    |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.931043948Z #16 43.05 |    |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.931045758Z #16 43.05 |    |         \--- org.springframework:spring-tx:6.2.12
2025-11-24T00:58:11.931047498Z #16 43.06 |    |              +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.931049268Z #16 43.06 |    |              \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.931050948Z #16 43.06 |    +--- org.hibernate.orm:hibernate-core:6.6.33.Final
2025-11-24T00:58:11.931052628Z #16 43.06 |    |    +--- jakarta.persistence:jakarta.persistence-api:3.1.0
2025-11-24T00:58:11.931054308Z #16 43.06 |    |    +--- jakarta.transaction:jakarta.transaction-api:2.0.1
2025-11-24T00:58:11.931056008Z #16 43.06 |    |    +--- org.jboss.logging:jboss-logging:3.5.0.Final -> 3.6.1.Final
2025-11-24T00:58:11.931057828Z #16 43.06 |    |    +--- org.hibernate.common:hibernate-commons-annotations:7.0.3.Final
2025-11-24T00:58:11.931060448Z #16 43.06 |    |    +--- io.smallrye:jandex:3.2.0
2025-11-24T00:58:11.931062238Z #16 43.06 |    |    +--- com.fasterxml:classmate:1.5.1 -> 1.7.1
2025-11-24T00:58:11.931064018Z #16 43.06 |    |    +--- net.bytebuddy:byte-buddy:1.15.11 -> 1.17.8
2025-11-24T00:58:11.931065708Z #16 43.06 |    |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.0 -> 4.0.4
2025-11-24T00:58:11.931067458Z #16 43.06 |    |    |    \--- jakarta.activation:jakarta.activation-api:2.1.4
2025-11-24T00:58:11.931069149Z #16 43.06 |    |    +--- org.glassfish.jaxb:jaxb-runtime:4.0.2 -> 4.0.6
2025-11-24T00:58:11.931070838Z #16 43.06 |    |    |    \--- org.glassfish.jaxb:jaxb-core:4.0.6
2025-11-24T00:58:11.931072629Z #16 43.06 |    |    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.4 (*)
2025-11-24T00:58:11.931074378Z #16 43.06 |    |    |         +--- jakarta.activation:jakarta.activation-api:2.1.4
2025-11-24T00:58:11.931076038Z #16 43.06 |    |    |         +--- org.eclipse.angus:angus-activation:2.0.3
2025-11-24T00:58:11.931077699Z #16 43.06 |    |    |         |    \--- jakarta.activation:jakarta.activation-api:2.1.4
2025-11-24T00:58:11.931079379Z #16 43.06 |    |    |         +--- org.glassfish.jaxb:txw2:4.0.6
2025-11-24T00:58:11.931085209Z #16 43.06 |    |    |         \--- com.sun.istack:istack-commons-runtime:4.1.2
2025-11-24T00:58:11.931087119Z #16 43.06 |    |    +--- jakarta.inject:jakarta.inject-api:2.0.1
2025-11-24T00:58:11.931088859Z #16 43.06 |    |    \--- org.antlr:antlr4-runtime:4.13.0
2025-11-24T00:58:11.931090789Z #16 43.06 |    +--- org.springframework.data:spring-data-jpa:3.5.5
2025-11-24T00:58:11.931092449Z #16 43.06 |    |    +--- org.springframework.data:spring-data-commons:3.5.5
2025-11-24T00:58:11.931094119Z #16 43.06 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.931095799Z #16 43.06 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.931097539Z #16 43.06 |    |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:11.931099219Z #16 43.06 |    |    +--- org.springframework:spring-orm:6.2.12
2025-11-24T00:58:11.931100899Z #16 43.06 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.931102669Z #16 43.06 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.931104329Z #16 43.06 |    |    |    +--- org.springframework:spring-jdbc:6.2.12 (*)
2025-11-24T00:58:11.931105999Z #16 43.06 |    |    |    \--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:11.931107679Z #16 43.06 |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.93110937Z #16 43.06 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.931111079Z #16 43.06 |    |    +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:11.931115319Z #16 43.06 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.93111707Z #16 43.06 |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.93111877Z #16 43.06 |    |    +--- org.antlr:antlr4-runtime:4.13.0
2025-11-24T00:58:11.93112045Z #16 43.06 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.0.0 -> 2.1.1
2025-11-24T00:58:11.93112214Z #16 43.06 |    |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:11.93112383Z #16 43.06 |    \--- org.springframework:spring-aspects:6.2.12
2025-11-24T00:58:11.93112549Z #16 43.06 |         \--- org.aspectj:aspectjweaver:1.9.22.1 -> 1.9.24
2025-11-24T00:58:11.93112717Z #16 43.06 +--- org.springframework.boot:spring-boot-starter-security -> 3.5.7
2025-11-24T00:58:11.93112885Z #16 43.06 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.93113051Z #16 43.06 |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.93113224Z #16 43.06 |    +--- org.springframework.security:spring-security-config:6.5.6
2025-11-24T00:58:11.93113393Z #16 43.06 |    |    +--- org.springframework.security:spring-security-core:6.5.6
2025-11-24T00:58:11.93113559Z #16 43.06 |    |    |    +--- org.springframework.security:spring-security-crypto:6.5.6
2025-11-24T00:58:11.93113728Z #16 43.06 |    |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.93113905Z #16 43.06 |    |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.93114073Z #16 43.06 |    |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.93114249Z #16 43.06 |    |    |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.93114415Z #16 43.06 |    |    |    +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:11.93114592Z #16 43.06 |    |    |    \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
2025-11-24T00:58:11.931147651Z #16 43.06 |    |    +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.93114932Z #16 43.06 |    |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.931150991Z #16 43.06 |    |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.93115354Z #16 43.06 |    |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.931155241Z #16 43.06 |    \--- org.springframework.security:spring-security-web:6.5.6
2025-11-24T00:58:11.931156931Z #16 43.06 |         +--- org.springframework.security:spring-security-core:6.5.6 (*)
2025-11-24T00:58:11.931158611Z #16 43.06 |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.931160341Z #16 43.06 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.931162021Z #16 43.06 |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.931163691Z #16 43.06 |         +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.931165351Z #16 43.06 |         +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:11.931167051Z #16 43.06 |         \--- org.springframework:spring-web:6.2.12
2025-11-24T00:58:11.931168741Z #16 43.06 |              +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.931170421Z #16 43.06 |              +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:11.931172141Z #16 43.06 |              \--- io.micrometer:micrometer-observation:1.14.12 -> 1.15.5 (*)
2025-11-24T00:58:11.931173861Z #16 43.06 +--- org.springframework.boot:spring-boot-starter-thymeleaf -> 3.5.7
2025-11-24T00:58:11.931175531Z #16 43.06 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.931179701Z #16 43.06 |    \--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE
2025-11-24T00:58:11.931181441Z #16 43.06 |         +--- org.thymeleaf:thymeleaf:3.1.3.RELEASE
2025-11-24T00:58:11.931183101Z #16 43.06 |         |    +--- org.attoparser:attoparser:2.0.7.RELEASE
2025-11-24T00:58:11.931184781Z #16 43.06 |         |    +--- org.unbescape:unbescape:1.1.6.RELEASE
2025-11-24T00:58:11.931186451Z #16 43.06 |         |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:11.931194332Z #16 43.06 |         \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:11.931196162Z #16 43.06 +--- org.springframework.boot:spring-boot-starter-validation -> 3.5.7
2025-11-24T00:58:11.931197822Z #16 43.06 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.931199472Z #16 43.06 |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
2025-11-24T00:58:11.931201342Z #16 43.06 |    \--- org.hibernate.validator:hibernate-validator:8.0.3.Final
2025-11-24T00:58:11.931203312Z #16 43.06 |         +--- jakarta.validation:jakarta.validation-api:3.0.2
2025-11-24T00:58:11.931205012Z #16 43.06 |         +--- org.jboss.logging:jboss-logging:3.4.3.Final -> 3.6.1.Final
2025-11-24T00:58:11.931206662Z #16 43.06 |         \--- com.fasterxml:classmate:1.5.1 -> 1.7.1
2025-11-24T00:58:11.931208432Z #16 43.06 +--- org.springframework.boot:spring-boot-starter-web -> 3.5.7
2025-11-24T00:58:11.931210082Z #16 43.06 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.931211772Z #16 43.06 |    +--- org.springframework.boot:spring-boot-starter-json:3.5.7
2025-11-24T00:58:11.931213442Z #16 43.06 |    |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:11.931215092Z #16 43.06 |    |    +--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:11.931216762Z #16 43.06 |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2
2025-11-24T00:58:11.931218912Z #16 43.06 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2
2025-11-24T00:58:11.931220572Z #16 43.06 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2
2025-11-24T00:58:11.931222442Z #16 43.06 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (c)
2025-11-24T00:58:11.931224112Z #16 43.06 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (c)
2025-11-24T00:58:11.931225802Z #16 43.06 |    |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (c)
2025-11-24T00:58:11.931227513Z #16 43.06 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2 (c)
2025-11-24T00:58:11.931229202Z #16 43.06 |    |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2 (c)
2025-11-24T00:58:11.931230882Z #16 43.06 |    |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2 (c)
2025-11-24T00:58:11.931232562Z #16 43.06 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2
2025-11-24T00:58:11.931234223Z #16 43.06 |    |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:11.931235923Z #16 43.06 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:11.931237593Z #16 43.06 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
2025-11-24T00:58:11.931239243Z #16 43.06 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:11.931240893Z #16 43.06 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:11.931242563Z #16 43.06 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:11.931244223Z #16 43.06 |    |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
2025-11-24T00:58:11.931248483Z #16 43.06 |    |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.19.2 (*)
2025-11-24T00:58:11.931250213Z #16 43.06 |    |    |    +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:11.931251923Z #16 43.06 |    |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:11.931253613Z #16 43.06 |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:11.931255283Z #16 43.06 |    |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
2025-11-24T00:58:11.931256963Z #16 43.06 |    |         +--- com.fasterxml.jackson.core:jackson-core:2.19.2 (*)
2025-11-24T00:58:11.931258633Z #16 43.06 |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.19.2 (*)
2025-11-24T00:58:11.931260303Z #16 43.06 |    |         \--- com.fasterxml.jackson:jackson-bom:2.19.2 (*)
2025-11-24T00:58:11.931262073Z #16 43.06 |    +--- org.springframework.boot:spring-boot-starter-tomcat:3.5.7
2025-11-24T00:58:11.931263953Z #16 43.06 |    |    +--- jakarta.annotation:jakarta.annotation-api:2.1.1
2025-11-24T00:58:11.931265723Z #16 43.06 |    |    +--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
2025-11-24T00:58:11.931267454Z #16 43.06 |    |    +--- org.apache.tomcat.embed:tomcat-embed-el:10.1.48
2025-11-24T00:58:11.931269203Z #16 43.06 |    |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:10.1.48
2025-11-24T00:58:11.931270903Z #16 43.06 |    |         \--- org.apache.tomcat.embed:tomcat-embed-core:10.1.48
2025-11-24T00:58:11.931272614Z #16 43.06 |    +--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:11.931274344Z #16 43.06 |    \--- org.springframework:spring-webmvc:6.2.12
2025-11-24T00:58:11.931276004Z #16 43.06 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:11.931277674Z #16 43.06 |         +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:11.931279384Z #16 43.06 |         +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:11.931281064Z #16 43.06 |         +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:12.167546449Z #16 43.06 |         +--- org.springframework:spring-expression:6.2.12 (*)
2025-11-24T00:58:12.1675692Z #16 43.06 |         \--- org.springframework:spring-web:6.2.12 (*)
2025-11-24T00:58:12.16757283Z #16 43.06 +--- org.springframework.retry:spring-retry -> 2.0.12
2025-11-24T00:58:12.16757588Z #16 43.06 +--- org.springframework:spring-aspects -> 6.2.12 (*)
2025-11-24T00:58:12.16757896Z #16 43.06 +--- org.thymeleaf.extras:thymeleaf-extras-springsecurity6 -> 3.1.3.RELEASE
2025-11-24T00:58:12.16758143Z #16 43.06 |    +--- org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE (*)
2025-11-24T00:58:12.16758373Z #16 43.06 |    \--- org.slf4j:slf4j-api:2.0.16 -> 2.0.17
2025-11-24T00:58:12.16758605Z #16 43.06 +--- org.springframework.boot:spring-boot-starter-data-redis -> 3.5.7
2025-11-24T00:58:12.16758834Z #16 43.06 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:12.16759057Z #16 43.06 |    +--- io.lettuce:lettuce-core:6.6.0.RELEASE
2025-11-24T00:58:12.16759281Z #16 43.06 |    |    +--- redis.clients.authentication:redis-authx-core:0.1.1-beta2
2025-11-24T00:58:12.16759516Z #16 43.06 |    |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
2025-11-24T00:58:12.16759751Z #16 43.06 |    |    +--- io.netty:netty-common:4.1.118.Final -> 4.1.128.Final
2025-11-24T00:58:12.167599851Z #16 43.06 |    |    +--- io.netty:netty-handler:4.1.118.Final -> 4.1.128.Final
2025-11-24T00:58:12.16760246Z #16 43.06 |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:12.167604871Z #16 43.06 |    |    |    +--- io.netty:netty-resolver:4.1.128.Final
2025-11-24T00:58:12.167619441Z #16 43.06 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:12.167621871Z #16 43.06 |    |    |    +--- io.netty:netty-buffer:4.1.128.Final
2025-11-24T00:58:12.167624221Z #16 43.06 |    |    |    |    \--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:12.167626591Z #16 43.06 |    |    |    +--- io.netty:netty-transport:4.1.128.Final
2025-11-24T00:58:12.167628871Z #16 43.06 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:12.167631371Z #16 43.06 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:12.167633831Z #16 43.06 |    |    |    |    \--- io.netty:netty-resolver:4.1.128.Final (*)
2025-11-24T00:58:12.167636131Z #16 43.06 |    |    |    +--- io.netty:netty-transport-native-unix-common:4.1.128.Final
2025-11-24T00:58:12.167638481Z #16 43.06 |    |    |    |    +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:12.167640781Z #16 43.06 |    |    |    |    +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:12.167643132Z #16 43.06 |    |    |    |    \--- io.netty:netty-transport:4.1.128.Final (*)
2025-11-24T00:58:12.167645492Z #16 43.06 |    |    |    \--- io.netty:netty-codec:4.1.128.Final
2025-11-24T00:58:12.167647882Z #16 43.06 |    |    |         +--- io.netty:netty-common:4.1.128.Final
2025-11-24T00:58:12.167650252Z #16 43.06 |    |    |         +--- io.netty:netty-buffer:4.1.128.Final (*)
2025-11-24T00:58:12.167652592Z #16 43.06 |    |    |         \--- io.netty:netty-transport:4.1.128.Final (*)
2025-11-24T00:58:12.167655092Z #16 43.06 |    |    +--- io.netty:netty-transport:4.1.118.Final -> 4.1.128.Final (*)
2025-11-24T00:58:12.167657412Z #16 43.06 |    |    \--- io.projectreactor:reactor-core:3.6.6 -> 3.7.12
2025-11-24T00:58:12.167659702Z #16 43.06 |    |         \--- org.reactivestreams:reactive-streams:1.0.4
2025-11-24T00:58:12.167662052Z #16 43.06 |    \--- org.springframework.data:spring-data-redis:3.5.5
2025-11-24T00:58:12.167664402Z #16 43.06 |         +--- org.springframework.data:spring-data-keyvalue:3.5.5
2025-11-24T00:58:12.167666772Z #16 43.06 |         |    +--- org.springframework.data:spring-data-commons:3.5.5 (*)
2025-11-24T00:58:12.167669312Z #16 43.06 |         |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:12.167671682Z #16 43.06 |         |    +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:12.167673962Z #16 43.06 |         |    \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:12.167676292Z #16 43.06 |         +--- org.springframework:spring-tx:6.2.12 (*)
2025-11-24T00:58:12.167678873Z #16 43.06 |         +--- org.springframework:spring-oxm:6.2.12
2025-11-24T00:58:12.167693903Z #16 43.06 |         |    +--- jakarta.xml.bind:jakarta.xml.bind-api:3.0.1 -> 4.0.4 (*)
2025-11-24T00:58:12.167696883Z #16 43.06 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:12.167699233Z #16 43.06 |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:12.167701483Z #16 43.06 |         +--- org.springframework:spring-aop:6.2.12 (*)
2025-11-24T00:58:12.167703753Z #16 43.06 |         +--- org.springframework:spring-context-support:6.2.12
2025-11-24T00:58:12.167706083Z #16 43.06 |         |    +--- org.springframework:spring-beans:6.2.12 (*)
2025-11-24T00:58:12.167708573Z #16 43.06 |         |    +--- org.springframework:spring-context:6.2.12 (*)
2025-11-24T00:58:12.167710923Z #16 43.06 |         |    \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:12.167715043Z #16 43.06 |         \--- org.slf4j:slf4j-api:2.0.2 -> 2.0.17
2025-11-24T00:58:12.167717583Z #16 43.06 +--- org.springframework.boot:spring-boot-starter-cache -> 3.5.7
2025-11-24T00:58:12.167725364Z #16 43.06 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:12.167727764Z #16 43.06 |    \--- org.springframework:spring-context-support:6.2.12 (*)
2025-11-24T00:58:12.167730854Z #16 43.06 +--- net.coobird:thumbnailator:0.4.19
2025-11-24T00:58:12.167733124Z #16 43.06 +--- com.cloudinary:cloudinary-http5:2.3.0
2025-11-24T00:58:12.167735504Z #16 43.06 |    +--- com.cloudinary:cloudinary-core:2.3.0
2025-11-24T00:58:12.167737834Z #16 43.06 |    +--- org.apache.commons:commons-lang3:3.1 -> 3.18.0
2025-11-24T00:58:12.167740164Z #16 43.06 |    +--- org.apache.httpcomponents.client5:httpclient5:5.3.1 -> 5.5.1
2025-11-24T00:58:12.167742474Z #16 43.06 |    |    +--- org.apache.httpcomponents.core5:httpcore5:5.3.6
2025-11-24T00:58:12.167745104Z #16 43.06 |    |    +--- org.apache.httpcomponents.core5:httpcore5-h2:5.3.6
2025-11-24T00:58:12.167747324Z #16 43.06 |    |    |    \--- org.apache.httpcomponents.core5:httpcore5:5.3.6
2025-11-24T00:58:12.167749614Z #16 43.06 |    |    \--- org.slf4j:slf4j-api:1.7.36 -> 2.0.17
2025-11-24T00:58:12.167751904Z #16 43.06 |    \--- org.apache.httpcomponents.core5:httpcore5:5.2.5 -> 5.3.6
2025-11-24T00:58:12.167754494Z #16 43.06 +--- io.github.openfeign.querydsl:querydsl-jpa:7.0
2025-11-24T00:58:12.167756755Z #16 43.06 |    \--- io.github.openfeign.querydsl:querydsl-core:7.0
2025-11-24T00:58:12.167758944Z #16 43.06 |         \--- io.projectreactor:reactor-core:3.7.6 -> 3.7.12 (*)
2025-11-24T00:58:12.167761235Z #16 43.06 +--- com.h2database:h2 -> 2.3.232
2025-11-24T00:58:12.167763645Z #16 43.06 +--- org.postgresql:postgresql -> 42.7.8
2025-11-24T00:58:12.167765995Z #16 43.06 |    \--- org.checkerframework:checker-qual:3.49.5
2025-11-24T00:58:12.167768225Z #16 43.06 +--- org.springframework.boot:spring-boot-starter-test -> 3.5.7
2025-11-24T00:58:12.167770435Z #16 43.06 |    +--- org.springframework.boot:spring-boot-starter:3.5.7 (*)
2025-11-24T00:58:12.167772735Z #16 43.06 |    +--- org.springframework.boot:spring-boot-test:3.5.7
2025-11-24T00:58:12.167774965Z #16 43.06 |    |    +--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:12.167777175Z #16 43.06 |    |    \--- org.springframework:spring-test:6.2.12
2025-11-24T00:58:12.167779465Z #16 43.06 |    |         \--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:12.167781865Z #16 43.06 |    +--- org.springframework.boot:spring-boot-test-autoconfigure:3.5.7
2025-11-24T00:58:12.167784135Z #16 43.06 |    |    +--- org.springframework.boot:spring-boot:3.5.7 (*)
2025-11-24T00:58:12.167786405Z #16 43.06 |    |    +--- org.springframework.boot:spring-boot-test:3.5.7 (*)
2025-11-24T00:58:12.167788595Z #16 43.06 |    |    \--- org.springframework.boot:spring-boot-autoconfigure:3.5.7 (*)
2025-11-24T00:58:12.167790875Z #16 43.06 |    +--- com.jayway.jsonpath:json-path:2.9.0
2025-11-24T00:58:12.167793115Z #16 43.06 |    |    +--- net.minidev:json-smart:2.5.0 -> 2.5.2
2025-11-24T00:58:12.167795365Z #16 43.06 |    |    |    \--- net.minidev:accessors-smart:2.5.2
2025-11-24T00:58:12.167797656Z #16 43.06 |    |    |         \--- org.ow2.asm:asm:9.7.1
2025-11-24T00:58:12.167799885Z #16 43.06 |    |    \--- org.slf4j:slf4j-api:2.0.11 -> 2.0.17
2025-11-24T00:58:12.167802106Z #16 43.06 |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.4 (*)
2025-11-24T00:58:12.167804286Z #16 43.06 |    +--- net.minidev:json-smart:2.5.2 (*)
2025-11-24T00:58:12.167806506Z #16 43.06 |    +--- org.assertj:assertj-core:3.27.6
2025-11-24T00:58:12.167808736Z #16 43.06 |    |    \--- net.bytebuddy:byte-buddy:1.17.7 -> 1.17.8
2025-11-24T00:58:12.167810946Z #16 43.06 |    +--- org.awaitility:awaitility:4.2.2
2025-11-24T00:58:12.167816886Z #16 43.06 |    |    \--- org.hamcrest:hamcrest:2.1 -> 3.0
2025-11-24T00:58:12.167819336Z #16 43.06 |    +--- org.hamcrest:hamcrest:3.0
2025-11-24T00:58:12.167821586Z #16 43.06 |    +--- org.junit.jupiter:junit-jupiter:5.12.2
2025-11-24T00:58:12.167823876Z #16 43.06 |    |    +--- org.junit:junit-bom:5.12.2
2025-11-24T00:58:12.167826116Z #16 43.06 |    |    |    +--- org.junit.jupiter:junit-jupiter:5.12.2 (c)
2025-11-24T00:58:12.167828326Z #16 43.06 |    |    |    +--- org.junit.jupiter:junit-jupiter-api:5.12.2 (c)
2025-11-24T00:58:12.167830646Z #16 43.06 |    |    |    +--- org.junit.jupiter:junit-jupiter-engine:5.12.2 (c)
2025-11-24T00:58:12.167832896Z #16 43.06 |    |    |    +--- org.junit.jupiter:junit-jupiter-params:5.12.2 (c)
2025-11-24T00:58:12.167835186Z #16 43.06 |    |    |    +--- org.junit.platform:junit-platform-engine:1.12.2 (c)
2025-11-24T00:58:12.167837526Z #16 43.06 |    |    |    +--- org.junit.platform:junit-platform-launcher:1.12.2 (c)
2025-11-24T00:58:12.167839837Z #16 43.06 |    |    |    \--- org.junit.platform:junit-platform-commons:1.12.2 (c)
2025-11-24T00:58:12.167842077Z #16 43.06 |    |    +--- org.junit.jupiter:junit-jupiter-api:5.12.2
2025-11-24T00:58:12.167844367Z #16 43.06 |    |    |    +--- org.junit:junit-bom:5.12.2 (*)
2025-11-24T00:58:12.167846717Z #16 43.06 |    |    |    +--- org.opentest4j:opentest4j:1.3.0
2025-11-24T00:58:12.167849027Z #16 43.06 |    |    |    \--- org.junit.platform:junit-platform-commons:1.12.2
2025-11-24T00:58:12.167877347Z #16 43.06 |    |    |         \--- org.junit:junit-bom:5.12.2 (*)
2025-11-24T00:58:12.167883278Z #16 43.06 |    |    +--- org.junit.jupiter:junit-jupiter-params:5.12.2
2025-11-24T00:58:12.167886408Z #16 43.06 |    |    |    +--- org.junit:junit-bom:5.12.2 (*)
2025-11-24T00:58:12.167888818Z #16 43.06 |    |    |    \--- org.junit.jupiter:junit-jupiter-api:5.12.2 (*)
2025-11-24T00:58:12.167891238Z #16 43.06 |    |    \--- org.junit.jupiter:junit-jupiter-engine:5.12.2
2025-11-24T00:58:12.167902888Z #16 43.06 |    |         +--- org.junit:junit-bom:5.12.2 (*)
2025-11-24T00:58:12.167905528Z #16 43.06 |    |         +--- org.junit.platform:junit-platform-engine:1.12.2
2025-11-24T00:58:12.167907958Z #16 43.06 |    |         |    +--- org.junit:junit-bom:5.12.2 (*)
2025-11-24T00:58:12.167910208Z #16 43.06 |    |         |    +--- org.opentest4j:opentest4j:1.3.0
2025-11-24T00:58:12.167912508Z #16 43.06 |    |         |    \--- org.junit.platform:junit-platform-commons:1.12.2 (*)
2025-11-24T00:58:12.167921349Z #16 43.06 |    |         \--- org.junit.jupiter:junit-jupiter-api:5.12.2 (*)
2025-11-24T00:58:12.167923819Z #16 43.06 |    +--- org.mockito:mockito-core:5.17.0
2025-11-24T00:58:12.167926149Z #16 43.06 |    |    +--- net.bytebuddy:byte-buddy:1.15.11 -> 1.17.8
2025-11-24T00:58:12.167928489Z #16 43.06 |    |    +--- net.bytebuddy:byte-buddy-agent:1.15.11 -> 1.17.8
2025-11-24T00:58:12.167930729Z #16 43.06 |    |    \--- org.objenesis:objenesis:3.3
2025-11-24T00:58:12.167933149Z #16 43.06 |    +--- org.mockito:mockito-junit-jupiter:5.17.0
2025-11-24T00:58:12.167935549Z #16 43.06 |    |    +--- org.mockito:mockito-core:5.17.0 (*)
2025-11-24T00:58:12.167937879Z #16 43.06 |    |    \--- org.junit.jupiter:junit-jupiter-api:5.11.4 -> 5.12.2 (*)
2025-11-24T00:58:12.167940199Z #16 43.06 |    +--- org.skyscreamer:jsonassert:1.5.3
2025-11-24T00:58:12.167942469Z #16 43.06 |    |    \--- com.vaadin.external.google:android-json:0.0.20131108.vaadin1
2025-11-24T00:58:12.167944789Z #16 43.06 |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:12.167947159Z #16 43.06 |    +--- org.springframework:spring-test:6.2.12 (*)
2025-11-24T00:58:12.167949469Z #16 43.06 |    +--- org.xmlunit:xmlunit-core:2.10.4
2025-11-24T00:58:12.167956289Z #16 43.06 |    \--- org.junit.platform:junit-platform-launcher -> 1.12.2
2025-11-24T00:58:12.16795875Z #16 43.06 |         +--- org.junit:junit-bom:5.12.2 (*)
2025-11-24T00:58:12.16796107Z #16 43.06 |         \--- org.junit.platform:junit-platform-engine:1.12.2 (*)
2025-11-24T00:58:12.16796335Z #16 43.06 +--- org.springframework.security:spring-security-test -> 6.5.6
2025-11-24T00:58:12.16796556Z #16 43.06 |    +--- org.springframework.security:spring-security-core:6.5.6 (*)
2025-11-24T00:58:12.16796781Z #16 43.06 |    +--- org.springframework.security:spring-security-web:6.5.6 (*)
2025-11-24T00:58:12.16797013Z #16 43.06 |    +--- org.springframework:spring-core:6.2.12 (*)
2025-11-24T00:58:12.16797246Z #16 43.06 |    \--- org.springframework:spring-test:6.2.12 (*)
2025-11-24T00:58:12.16797471Z #16 43.06 \--- org.junit.platform:junit-platform-launcher -> 1.12.2 (*)
2025-11-24T00:58:12.16797702Z #16 43.06
2025-11-24T00:58:12.16797934Z #16 43.06 testRuntimeOnly - Runtime only dependencies for source set 'test'. (n)
2025-11-24T00:58:12.16798167Z #16 43.06 \--- org.junit.platform:junit-platform-launcher (n)
2025-11-24T00:58:12.16798393Z #16 43.06
2025-11-24T00:58:12.16798841Z #16 43.06 (c) - A dependency constraint, not a dependency. The dependency affected by the constraint occurs elsewhere in the tree.
2025-11-24T00:58:12.16799225Z #16 43.06 (*) - Indicates repeated occurrences of a transitive dependency subtree. Gradle expands transitive dependency subtrees only once per project; repeat occurrences only display the root of the subtree, followed by this annotation.
2025-11-24T00:58:12.16799463Z #16 43.06
2025-11-24T00:58:12.167997081Z #16 43.06 (n) - A dependency or dependency configuration that cannot be resolved.
2025-11-24T00:58:12.167999441Z #16 43.06
2025-11-24T00:58:12.168001691Z #16 43.06 A web-based, searchable dependency report is available by adding the --scan option.
2025-11-24T00:58:12.168003921Z #16 43.15
2025-11-24T00:58:12.168006321Z #16 43.15 BUILD SUCCESSFUL in 42s
2025-11-24T00:58:12.168008731Z #16 43.15 1 actionable task: 1 executed
2025-11-24T00:58:12.45940052Z #16 DONE 43.6s
2025-11-24T00:58:12.612332923Z
2025-11-24T00:58:12.612355724Z #17 [builder 6/7] COPY src src/
2025-11-24T00:58:12.663421484Z #17 DONE 0.2s
2025-11-24T00:58:12.816255755Z
2025-11-24T00:58:12.816284376Z #18 [builder 7/7] RUN gradle bootJar --no-daemon -x test
2025-11-24T00:58:13.531263239Z #18 0.865 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
2025-11-24T00:58:15.132205019Z #18 2.466 Daemon will be stopped at the end of the build
2025-11-24T00:58:57.132401968Z #18 44.47 > Task :compileJava
2025-11-24T00:59:07.430493917Z #18 54.76 Note: Some input files use or override a deprecated API.
2025-11-24T00:59:07.626714433Z #18 54.76 Note: Recompile with -Xlint:deprecation for details.
2025-11-24T00:59:07.626748844Z #18 54.77 Note: /app/src/main/java/com/recipemate/global/util/ImageUploadUtil.java uses unchecked or unsafe operations.
2025-11-24T00:59:07.626756464Z #18 54.77 Note: Recompile with -Xlint:unchecked for details.
2025-11-24T00:59:24.730545416Z #18 72.06
2025-11-24T00:59:24.730629508Z #18 72.06 > Task :processResources
2025-11-24T00:59:24.730635359Z #18 72.06 > Task :classes
2025-11-24T00:59:25.13092635Z #18 72.46 > Task :resolveMainClassName
2025-11-24T00:59:33.829921937Z #18 81.16 > Task :bootJar
2025-11-24T00:59:34.080346532Z #18 81.26
2025-11-24T00:59:34.080369503Z #18 81.26 BUILD SUCCESSFUL in 1m 21s
2025-11-24T00:59:34.080374593Z #18 81.26 4 actionable tasks: 4 executed
2025-11-24T00:59:34.912379286Z #18 DONE 82.2s
2025-11-24T00:59:40.753509811Z
2025-11-24T00:59:40.753530612Z #19 [stage-1 5/5] COPY --from=builder /app/build/libs/*.jar app.jar
2025-11-24T00:59:53.232829956Z #19 DONE 12.5s
2025-11-24T00:59:53.389013003Z
2025-11-24T00:59:53.389036363Z #20 exporting to docker image format
2025-11-24T00:59:53.389040364Z #20 exporting layers
2025-11-24T00:59:57.128183002Z #20 exporting layers 3.9s done
2025-11-24T00:59:57.32525845Z #20 exporting manifest sha256:cd6ed03cd57eca2d442f905d1f0820b6ee91b1d625ed39be30b60248ea61fb87 0.0s done
2025-11-24T00:59:57.32527797Z #20 exporting config sha256:a2e54c3b82915b08bcff55d6b7612937afc5208aeb24bdc0e5549feafd62a558 0.0s done
2025-11-24T00:59:58.094828583Z #20 DONE 4.9s
2025-11-24T00:59:58.094845374Z
2025-11-24T00:59:58.094849124Z #21 exporting cache to client directory
2025-11-24T00:59:58.094852084Z #21 preparing build cache for export
2025-11-24T01:00:23.216084164Z #21 writing cache image manifest sha256:c31008bef9b42784883bd47a54a4359368d362dac8a824d77973658197dfaff9
2025-11-24T01:00:24.029222506Z #21 writing cache image manifest sha256:c31008bef9b42784883bd47a54a4359368d362dac8a824d77973658197dfaff9 0.8s done
2025-11-24T01:00:24.029249057Z #21 DONE 25.9s
2025-11-24T01:00:33.097121457Z Pushing image to registry...
2025-11-24T01:00:38.615618189Z Upload succeeded
2025-11-24T01:00:51.602496778Z ==> Deploying...
2025-11-24T01:01:22.562659677Z  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
2025-11-24T01:01:22.562662067Z   '  |____| .__|_| |_|_| |_\__, | / / / /
2025-11-24T01:01:22.562664567Z  =========|_|==============|___/=/_/_/_/
2025-11-24T01:01:22.562666697Z
2025-11-24T01:01:22.562992106Z  :: Spring Boot ::                (v3.5.7)
2025-11-24T01:01:22.562996646Z
2025-11-24T01:01:23.765451656Z 2025-11-24T01:01:23.762Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : Starting RecipeMateApplication v0.0.1-SNAPSHOT using Java 21.0.9 with PID 1 (/app/app.jar started by spring in /app)
2025-11-24T01:01:23.766175226Z 2025-11-24T01:01:23.765Z  INFO 1 --- [RecipeMate] [           main] com.recipemate.RecipeMateApplication     : The following 2 profiles are active: "dev.local", "prod"
2025-11-24T01:01:43.565714747Z 2025-11-24T01:01:43.565Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2025-11-24T01:01:43.567557438Z 2025-11-24T01:01:43.567Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2025-11-24T01:01:48.862658881Z 2025-11-24T01:01:48.862Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 5098 ms. Found 25 JPA repository interfaces.
2025-11-24T01:01:49.166407676Z 2025-11-24T01:01:49.166Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2025-11-24T01:01:49.265739848Z 2025-11-24T01:01:49.265Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2025-11-24T01:01:49.663210538Z 2025-11-24T01:01:49.662Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.badge.repository.BadgeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.665999696Z 2025-11-24T01:01:49.665Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.comment.repository.CommentRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.666016736Z 2025-11-24T01:01:49.665Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.directmessage.repository.DirectMessageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.666031237Z 2025-11-24T01:01:49.665Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.666043387Z 2025-11-24T01:01:49.665Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.GroupBuyRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.666323385Z 2025-11-24T01:01:49.666Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.groupbuy.repository.ParticipationRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.666572862Z 2025-11-24T01:01:49.666Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.like.repository.CommentLikeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.761265654Z 2025-11-24T01:01:49.760Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.like.repository.PostLikeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.76181622Z 2025-11-24T01:01:49.761Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.notification.repository.NotificationRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.762073527Z 2025-11-24T01:01:49.761Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.post.repository.PostImageRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.762356295Z 2025-11-24T01:01:49.762Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.post.repository.PostRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.76257085Z 2025-11-24T01:01:49.762Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeCorrectionRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.863781414Z 2025-11-24T01:01:49.862Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeIngredientRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.863812565Z 2025-11-24T01:01:49.863Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.86400079Z 2025-11-24T01:01:49.863Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipe.repository.RecipeStepRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.864400621Z 2025-11-24T01:01:49.864Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.recipewishlist.repository.RecipeWishlistRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.864767062Z 2025-11-24T01:01:49.864Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.report.repository.ReportRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.865118551Z 2025-11-24T01:01:49.864Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.review.repository.ReviewRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.865494132Z 2025-11-24T01:01:49.865Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.search.repository.SearchKeywordRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.865951215Z 2025-11-24T01:01:49.865Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.AddressRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.866204882Z 2025-11-24T01:01:49.865Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.MannerTempHistoryRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.86649401Z 2025-11-24T01:01:49.866Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.PersistentTokenJpaRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.960458372Z 2025-11-24T01:01:49.866Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.PointHistoryRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.961349767Z 2025-11-24T01:01:49.961Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.user.repository.UserRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.961735207Z 2025-11-24T01:01:49.961Z  INFO 1 --- [RecipeMate] [           main] .RepositoryConfigurationExtensionSupport : Spring Data Redis - Could not safely identify store assignment for repository candidate interface com.recipemate.domain.wishlist.repository.WishlistRepository; If you want this repository to be a Redis repository, consider annotating your entities with one of these annotations: org.springframework.data.redis.core.RedisHash (preferred), or consider extending one of the following types with your repository: org.springframework.data.keyvalue.repository.KeyValueRepository
2025-11-24T01:01:49.961906172Z 2025-11-24T01:01:49.961Z  INFO 1 --- [RecipeMate] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 593 ms. Found 0 Redis repository interfaces.
2025-11-24T01:02:03.597668017Z ==> No open ports detected, continuing to scan...
2025-11-24T01:02:04.037312123Z ==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
2025-11-24T01:02:07.564185595Z 2025-11-24T01:02:07.563Z  INFO 1 --- [RecipeMate] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2025-11-24T01:02:07.763481966Z 2025-11-24T01:02:07.763Z  INFO 1 --- [RecipeMate] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-11-24T01:02:07.766671425Z 2025-11-24T01:02:07.763Z  INFO 1 --- [RecipeMate] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.48]
2025-11-24T01:02:09.367616403Z 2025-11-24T01:02:09.367Z  INFO 1 --- [RecipeMate] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-11-24T01:02:09.46063899Z 2025-11-24T01:02:09.368Z  INFO 1 --- [RecipeMate] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 44405 ms
2025-11-24T01:02:16.563151601Z 2025-11-24T01:02:16.561Z  INFO 1 --- [RecipeMate] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2025-11-24T01:02:17.363913214Z 2025-11-24T01:02:17.363Z  INFO 1 --- [RecipeMate] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.33.Final
2025-11-24T01:02:18.061020434Z 2025-11-24T01:02:18.060Z  INFO 1 --- [RecipeMate] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2025-11-24T01:02:23.062531455Z 2025-11-24T01:02:23.062Z  INFO 1 --- [RecipeMate] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2025-11-24T01:02:23.566319091Z 2025-11-24T01:02:23.565Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-11-24T01:02:28.469274021Z 2025-11-24T01:02:28.468Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@1e160a9e
2025-11-24T01:02:28.562171504Z 2025-11-24T01:02:28.561Z  INFO 1 --- [RecipeMate] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-11-24T01:02:29.264795898Z 2025-11-24T01:02:29.264Z  WARN 1 --- [RecipeMate] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2025-11-24T01:02:29.962757642Z 2025-11-24T01:02:29.961Z  INFO 1 --- [RecipeMate] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
2025-11-24T01:02:29.962779533Z 	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
2025-11-24T01:02:29.962783133Z 	Database driver: undefined/unknown
2025-11-24T01:02:29.962786153Z 	Database version: 18.1
2025-11-24T01:02:29.962788473Z 	Autocommit mode: undefined/unknown
2025-11-24T01:02:29.962790763Z 	Isolation level: undefined/unknown
2025-11-24T01:02:29.962793113Z 	Minimum pool size: undefined/unknown
2025-11-24T01:02:29.962795643Z 	Maximum pool size: undefined/unknown
2025-11-24T01:02:36.067208116Z 2025-11-24T01:02:36.066Z  WARN 1 --- [RecipeMate] [           main] o.h.boot.model.internal.ToOneBinder      : HHH000491: 'com.recipemate.domain.recipewishlist.entity.RecipeWishlist.recipe' uses both @NotFound and FetchType.LAZY. @ManyToOne and @OneToOne associations mapped with @NotFound are forced to EAGER fetching.
2025-11-24T01:03:03.369192584Z 2025-11-24T01:03:03.368Z  INFO 1 --- [RecipeMate] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2025-11-24T01:03:04.066096179Z 2025-11-24T01:03:04.065Z  INFO 1 --- [RecipeMate] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2025-11-24T01:03:07.068272535Z ==> No open ports detected, continuing to scan...
2025-11-24T01:03:07.47962986Z ==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
2025-11-24T01:03:11.76463948Z 2025-11-24T01:03:11.764Z  INFO 1 --- [RecipeMate] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2025-11-24T01:04:10.44427487Z ==> No open ports detected, continuing to scan...
2025-11-24T01:04:10.886791816Z ==> Docs on specifying a port: https://render.com/docs/web-services#port-binding
2025-11-24T01:04:29.964661017Z 2025-11-24T01:04:29.964Z  WARN 1 --- [RecipeMate] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2025-11-24T01:04:30.765856962Z 2025-11-24T01:04:30.765Z  INFO 1 --- [RecipeMate] [           main] o.s.b.a.w.s.WelcomePageHandlerMapping    : Adding welcome page template: index
2025-11-24T01:04:35.56482802Z 2025-11-24T01:04:35.564Z  INFO 1 --- [RecipeMate] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name customUserDetailsService
2025-11-24T01:04:40.895723677Z ==> Out of memory (used over 512Mi)
2025-11-24T01:04:41.071958586Z ==> Common ways to troubleshoot your deploy: https://render.com/docs/troubleshooting-deploys