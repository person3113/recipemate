2025-11-24T01:50:39.58962597Z ==> Cloning from https://github.com/person3113/recipemate
2025-11-24T01:50:41.042158623Z ==> Checking out commit 698243f8a277dfa41f0760ae0964dd65853afe5f in branch develop
2025-11-24T01:50:43.931542675Z #1 [internal] load build definition from Dockerfile
2025-11-24T01:50:43.931583556Z #1 transferring dockerfile: 1.35kB done
2025-11-24T01:50:43.931587236Z #1 DONE 0.0s
2025-11-24T01:50:43.931589936Z
2025-11-24T01:50:43.931593426Z #2 [internal] load metadata for docker.io/library/eclipse-temurin:21-jre-alpine
2025-11-24T01:50:45.0619884Z #2 ...
2025-11-24T01:50:45.062002951Z
2025-11-24T01:50:45.062006991Z #3 [auth] library/eclipse-temurin:pull render-prod/docker-mirror-repository/library/eclipse-temurin:pull token for us-west1-docker.pkg.dev
2025-11-24T01:50:45.062011811Z #3 DONE 0.0s
2025-11-24T01:50:45.062013831Z
2025-11-24T01:50:45.062016731Z #4 [auth] library/gradle:pull render-prod/docker-mirror-repository/library/gradle:pull token for us-west1-docker.pkg.dev
2025-11-24T01:50:45.062018891Z #4 DONE 0.0s
2025-11-24T01:50:45.211166506Z
2025-11-24T01:50:45.211191037Z #5 [internal] load metadata for docker.io/library/gradle:8.5-jdk21-alpine
2025-11-24T01:50:48.986469205Z #5 ...
2025-11-24T01:50:48.986486845Z
2025-11-24T01:50:48.986490215Z #2 [internal] load metadata for docker.io/library/eclipse-temurin:21-jre-alpine
2025-11-24T01:50:48.986492725Z #2 DONE 5.2s
2025-11-24T01:50:49.137000263Z
2025-11-24T01:50:49.137019624Z #5 [internal] load metadata for docker.io/library/gradle:8.5-jdk21-alpine
2025-11-24T01:50:49.326420738Z #5 DONE 5.5s
2025-11-24T01:50:49.493846399Z
2025-11-24T01:50:49.493875379Z #6 [internal] load .dockerignore
2025-11-24T01:50:49.4938814Z #6 transferring context: 735B done
2025-11-24T01:50:49.49388411Z #6 DONE 0.0s
2025-11-24T01:50:49.49388644Z
2025-11-24T01:50:49.49389084Z #7 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T01:50:49.49389378Z #7 resolve docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87 0.0s done
2025-11-24T01:50:49.705184236Z #7 ...
2025-11-24T01:50:49.705199796Z
2025-11-24T01:50:49.705203117Z #8 [internal] load build context
2025-11-24T01:50:49.705206387Z #8 transferring context: 1.75MB 0.1s done
2025-11-24T01:50:49.705208697Z #8 DONE 0.2s
2025-11-24T01:50:49.705210717Z
2025-11-24T01:50:49.705213827Z #7 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T01:50:50.862061112Z #7 sha256:1b2010acb2c2f72b7c96a939846c10d9796df96c67f60b271c88f62010e4b002 0B / 167B 0.2s
2025-11-24T01:50:51.731536557Z #7 sha256:1b2010acb2c2f72b7c96a939846c10d9796df96c67f60b271c88f62010e4b002 167B / 167B 1.1s done
2025-11-24T01:50:51.882452224Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 0B / 132.54MB 0.2s
2025-11-24T01:50:52.114590697Z #7 sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 0B / 34.82MB 0.2s
2025-11-24T01:50:54.964748744Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 8.39MB / 132.54MB 3.2s
2025-11-24T01:50:54.964775124Z #7 sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 8.39MB / 34.82MB 3.0s
2025-11-24T01:50:55.331846284Z #7 sha256:516905b70d0910e39f224893c8efc6801b1e1d053bf0031afdb3ef18728a4088 0B / 1.33kB 0.2s
2025-11-24T01:50:55.632843679Z #7 sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 25.17MB / 34.82MB 3.6s
2025-11-24T01:50:56.232457531Z #7 sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 0B / 716B 0.2s
2025-11-24T01:50:56.382702842Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 33.55MB / 132.54MB 4.7s
2025-11-24T01:50:56.382726563Z #7 sha256:516905b70d0910e39f224893c8efc6801b1e1d053bf0031afdb3ef18728a4088 1.33kB / 1.33kB 1.1s done
2025-11-24T01:50:56.532060892Z #7 sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 34.82MB / 34.82MB 4.5s
2025-11-24T01:50:56.544455078Z #7 sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 0B / 179B 0.2s
2025-11-24T01:50:56.639158815Z #7 sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 34.82MB / 34.82MB 4.7s done
2025-11-24T01:50:56.639172115Z #7 sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 179B / 179B 0.3s
2025-11-24T01:50:56.763169711Z #7 sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 179B / 179B 0.4s done
2025-11-24T01:50:56.912947561Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 0B / 158.61MB 0.2s
2025-11-24T01:50:56.912960511Z #7 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 0B / 13.14MB 0.2s
2025-11-24T01:50:57.063557611Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 41.94MB / 132.54MB 5.3s
2025-11-24T01:50:57.390499034Z #7 sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 716B / 716B 1.2s done
2025-11-24T01:50:57.540643463Z #7 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 0B / 3.41MB 0.2s
2025-11-24T01:50:58.290377624Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 58.72MB / 132.54MB 6.5s
2025-11-24T01:50:58.740642856Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 67.11MB / 132.54MB 6.9s
2025-11-24T01:50:59.390083386Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 8.39MB / 158.61MB 2.7s
2025-11-24T01:50:59.390108447Z #7 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 8.39MB / 13.14MB 2.6s
2025-11-24T01:50:59.390111847Z #7 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 3.41MB / 3.41MB 2.0s done
2025-11-24T01:50:59.390114187Z #7 extracting sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8
2025-11-24T01:50:59.490430378Z #7 extracting sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 0.1s done
2025-11-24T01:50:59.490452329Z #7 ...
2025-11-24T01:50:59.490456229Z
2025-11-24T01:50:59.490460599Z #9 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T01:50:59.490463729Z #9 resolve docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11 0.0s done
2025-11-24T01:50:59.490467149Z #9 sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 2.28kB / 2.28kB 0.3s done
2025-11-24T01:50:59.490470069Z #9 sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 128B / 128B 1.1s done
2025-11-24T01:50:59.490472829Z #9 sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 53.17MB / 53.17MB 6.4s done
2025-11-24T01:50:59.490475529Z #9 sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 16.29MB / 16.29MB 5.5s done
2025-11-24T01:50:59.490478299Z #9 sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 3.80MB / 3.80MB 2.1s done
2025-11-24T01:50:59.49048173Z #9 extracting sha256:2d35ebdb57d9971fea0cac1582aa78935adf8058b2cc32db163c98822e5dfa1b 0.1s done
2025-11-24T01:50:59.49048528Z #9 extracting sha256:93902f35c01693819c190c354786cbb573f4ef49a5b865d6c34f2197839cc3e4 4.0s done
2025-11-24T01:50:59.49048894Z #9 extracting sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a
2025-11-24T01:50:59.639790578Z #9 ...
2025-11-24T01:50:59.639810749Z
2025-11-24T01:50:59.639814319Z #7 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T01:50:59.79046236Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 80.74MB / 132.54MB 8.0s
2025-11-24T01:50:59.79048244Z #7 sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 13.14MB / 13.14MB 3.0s done
2025-11-24T01:50:59.790485861Z #7 extracting sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b
2025-11-24T01:51:00.540760884Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 99.61MB / 132.54MB 8.7s
2025-11-24T01:51:00.69034235Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 16.78MB / 158.61MB 4.1s
2025-11-24T01:51:00.990673418Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 33.55MB / 158.61MB 4.4s
2025-11-24T01:51:01.439907436Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 109.05MB / 132.54MB 9.6s
2025-11-24T01:51:01.740808229Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 58.72MB / 158.61MB 5.1s
2025-11-24T01:51:02.190124528Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 130.02MB / 132.54MB 10.4s
2025-11-24T01:51:02.595335497Z #7 ...
2025-11-24T01:51:02.615320994Z
2025-11-24T01:51:02.615330584Z #9 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T01:51:02.615335744Z #9 extracting sha256:3a857bcc1a1505748a54fef25bb81f303468a26d3de127b6b0044b1ffde6275a 3.5s done
2025-11-24T01:51:02.615341384Z #9 DONE 13.2s
2025-11-24T01:51:02.615361315Z
2025-11-24T01:51:02.615365295Z #7 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T01:51:02.790019498Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 75.50MB / 158.61MB 6.2s
2025-11-24T01:51:02.940514095Z #7 sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 132.54MB / 132.54MB 11.0s done
2025-11-24T01:51:03.390153452Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 83.89MB / 158.61MB 6.8s
2025-11-24T01:51:03.390175173Z #7 extracting sha256:59648cfc069f04a8d0eece9cae80e25155888dc9b5de722b58603efafaa0d78b 3.6s done
2025-11-24T01:51:03.690666246Z #7 ...
2025-11-24T01:51:03.690688726Z
2025-11-24T01:51:03.690694226Z #9 [stage-1 1/5] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:326837fba06a8ff5482a17bafbd65319e64a6e997febb7c85ebe7e3f73c12b11
2025-11-24T01:51:03.690698946Z #9 extracting sha256:15a75c308165465141d861c1b7f7ec799b18fc84d5d8b87396dc29c3aa4894ff 1.0s done
2025-11-24T01:51:03.690702556Z #9 extracting sha256:23d1cf1d02f064d13d647e4816e26b17678a634769ec583b4eb29d5a68d415f7 0.0s done
2025-11-24T01:51:03.690706026Z #9 DONE 14.2s
2025-11-24T01:51:03.690709316Z
2025-11-24T01:51:03.690713347Z #10 [stage-1 2/5] WORKDIR /app
2025-11-24T01:51:03.690716867Z #10 DONE 0.0s
2025-11-24T01:51:03.690720087Z
2025-11-24T01:51:03.690723507Z #7 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T01:51:03.828714056Z #7 ...
2025-11-24T01:51:03.828735997Z
2025-11-24T01:51:03.828740546Z #11 [stage-1 3/5] RUN addgroup -S spring && adduser -S spring -G spring
2025-11-24T01:51:03.828744747Z #11 DONE 0.1s
2025-11-24T01:51:03.828748167Z
2025-11-24T01:51:03.828751767Z #12 [stage-1 4/5] RUN mkdir -p /app/uploads && chown -R spring:spring /app
2025-11-24T01:51:03.828755367Z #12 DONE 0.1s
2025-11-24T01:51:03.990209425Z
2025-11-24T01:51:03.990295168Z #7 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T01:51:03.990300917Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 93.17MB / 158.61MB 7.4s
2025-11-24T01:51:04.14058254Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 117.44MB / 158.61MB 7.5s
2025-11-24T01:51:04.740587052Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 134.22MB / 158.61MB 8.1s
2025-11-24T01:51:05.340776867Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 142.61MB / 158.61MB 8.7s
2025-11-24T01:51:05.905256832Z #7 sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 158.61MB / 158.61MB 9.3s done
2025-11-24T01:51:06.090067148Z #7 extracting sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e
2025-11-24T01:51:08.635402508Z #7 extracting sha256:6b168e20b9f2c7cde9300b36733340d229efb681e7862b716580cdb07cdb572e 2.7s done
2025-11-24T01:51:08.635431199Z #7 DONE 19.3s
2025-11-24T01:51:08.86285471Z
2025-11-24T01:51:08.86288027Z #7 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T01:51:08.86288456Z #7 extracting sha256:98795b95bb4830f88ab5d81f355f8a6f3bd833705d858023f9b58c42457db454 0.0s done
2025-11-24T01:51:08.862887831Z #7 extracting sha256:c4451c8b3dbe1616b4f1d08ac6cbd135f48e3d3393f2c37402ac18b0b845dfdc 0.0s done
2025-11-24T01:51:08.862890621Z #7 extracting sha256:516905b70d0910e39f224893c8efc6801b1e1d053bf0031afdb3ef18728a4088 0.0s done
2025-11-24T01:51:08.862893441Z #7 extracting sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd
2025-11-24T01:51:09.573203822Z #7 extracting sha256:1e25bf6602788516b96f9136306fc7eae2cee151d94343b0d47023e5b36ed5bd 0.9s done
2025-11-24T01:51:09.573239143Z #7 DONE 20.2s
2025-11-24T01:51:09.723616797Z
2025-11-24T01:51:09.723640108Z #7 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T01:51:09.723645237Z #7 extracting sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96
2025-11-24T01:51:10.576623329Z #7 extracting sha256:38c3a1b03835788cfba7bd295627422fb24ce2d03897cc5df857e07ebba62e96 1.0s done
2025-11-24T01:51:10.576638979Z #7 DONE 21.2s
2025-11-24T01:51:10.718408318Z
2025-11-24T01:51:10.718431129Z #7 [builder 1/7] FROM docker.io/library/gradle:8.5-jdk21-alpine@sha256:22951b48bcba236869333aad540d08a63203ebf91a3f366308dab6ecf748af87
2025-11-24T01:51:10.718437599Z #7 extracting sha256:1b2010acb2c2f72b7c96a939846c10d9796df96c67f60b271c88f62010e4b002 0.0s done
2025-11-24T01:51:10.718442359Z #7 DONE 21.2s
2025-11-24T01:51:10.718446499Z
2025-11-24T01:51:10.718451249Z #13 [builder 2/7] WORKDIR /app
2025-11-24T01:51:10.718455719Z #13 DONE 0.0s
2025-11-24T01:51:10.71846006Z
2025-11-24T01:51:10.71846488Z #14 [builder 3/7] COPY build.gradle settings.gradle gradlew ./
2025-11-24T01:51:10.71846908Z #14 DONE 0.1s
2025-11-24T01:51:10.71847307Z
2025-11-24T01:51:10.71847751Z #15 [builder 4/7] COPY gradle gradle/
2025-11-24T01:51:10.71849023Z #15 DONE 0.0s
2025-11-24T01:51:10.871228301Z
2025-11-24T01:51:10.871254182Z #16 [builder 5/7] RUN gradle dependencies --no-daemon || true
2025-11-24T01:51:11.39558198Z #16 0.675
2025-11-24T01:51:11.645101967Z #16 0.675 Welcome to Gradle 8.5!
2025-11-24T01:51:11.645125298Z #16 0.675
2025-11-24T01:51:11.645130068Z #16 0.675 Here are the highlights of this release:
2025-11-24T01:51:11.645134168Z #16 0.675  - Support for running on Java 21
2025-11-24T01:51:11.645152638Z #16 0.675  - Faster first use with Kotlin DSL
2025-11-24T01:51:11.645154848Z #16 0.676  - Improved error and warning messages
2025-11-24T01:51:11.645156898Z #16 0.676
2025-11-24T01:51:11.645159598Z #16 0.676 For more details see https://docs.gradle.org/8.5/release-notes.html
2025-11-24T01:51:11.645161698Z #16 0.676
2025-11-24T01:51:11.645164529Z #16 0.774 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
2025-11-24T01:51:12.894461497Z #16 2.174 Daemon will be stopped at the end of the build
2025-11-24T01:51:36.094613546Z #16 25.37
2025-11-24T01:51:36.247154292Z #16 25.38 > Task :dependencies
2025-11-24T01:51:36.247171993Z #16 25.38
2025-11-24T01:51:36.247175793Z #16 25.38
2025-11-24T01:51:51.405405313Z
2025-11-24T01:51:51.405426393Z #17 [builder 6/7] COPY src src/
2025-11-24T01:52:05.833727455Z #17 DONE 14.4s
2025-11-24T01:52:06.05418202Z
2025-11-24T01:52:06.058474442Z #18 [builder 7/7] RUN gradle bootJar --no-daemon -x test
2025-11-24T01:52:08.257022886Z #18 2.354 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
2025-11-24T01:52:09.756273552Z #18 3.853 Daemon will be stopped at the end of the build
2025-11-24T01:52:51.593347912Z #18 45.69 > Task :compileJava
2025-11-24T01:52:59.990092494Z #18 53.89 Note: Some input files use or override a deprecated API.
2025-11-24T01:52:59.990116905Z #18 53.89 Note: Recompile with -Xlint:deprecation for details.
2025-11-24T01:52:59.990122865Z #18 53.89 Note: /app/src/main/java/com/recipemate/global/util/ImageUploadUtil.java uses unchecked or unsafe operations.
2025-11-24T01:52:59.990127865Z #18 53.89 Note: Recompile with -Xlint:unchecked for details.
2025-11-24T01:53:18.091069473Z #18 72.19
2025-11-24T01:53:18.241607181Z #18 72.19 > Task :processResources
2025-11-24T01:53:18.241630562Z #18 72.19 > Task :classes
2025-11-24T01:53:18.390579902Z #18 72.49 > Task :resolveMainClassName
2025-11-24T01:53:26.456144409Z #18 80.55 > Task :bootJar
2025-11-24T01:53:26.68732364Z #18 80.55
2025-11-24T01:53:26.687369891Z #18 80.55 BUILD SUCCESSFUL in 1m 18s
2025-11-24T01:53:26.687384692Z #18 80.55 4 actionable tasks: 4 executed
2025-11-24T01:53:27.212516769Z #18 DONE 81.3s
2025-11-24T01:53:27.897176628Z
2025-11-24T01:53:27.897204758Z #19 [stage-1 5/5] COPY --from=builder /app/build/libs/*.jar app.jar
2025-11-24T01:53:35.330096836Z #19 DONE 7.4s
2025-11-24T01:53:35.485316955Z
2025-11-24T01:53:35.485336906Z #20 exporting to docker image format
2025-11-24T01:53:35.485344166Z #20 exporting layers
2025-11-24T01:53:37.304424215Z #20 exporting layers 2.0s done
2025-11-24T01:53:37.490100021Z #20 exporting manifest sha256:4fa0fb97474bf92a31e189a58b7ea511f284be897ad3e1cf31cde122eaba768d 0.0s done
2025-11-24T01:53:37.490128001Z #20 exporting config sha256:c0f9705f12fdedede0f5037ed13b2092f37bf6476a46534ad0f352587a74ca53 0.0s done
2025-11-24T01:53:38.2091689Z #20 DONE 2.9s
2025-11-24T01:53:38.359492903Z
2025-11-24T01:53:38.359516074Z #21 exporting cache to client directory
2025-11-24T01:53:38.359521674Z #21 preparing build cache for export
2025-11-24T01:53:49.727926115Z #21 writing cache image manifest sha256:b9368ee2c755f3a5b9eb41c01760f59483aac81a7f26f75e25a498bb979ad4ec done
2025-11-24T01:53:49.727949396Z #21 DONE 11.5s
2025-11-24T01:53:51.780592682Z Pushing image to registry...
2025-11-24T01:53:55.289888068Z Upload succeeded
2025-11-24T01:53:59.252880584Z ==> Deploying...
2025-11-24T01:54:23.607725208Z 01:54:23.603 [main] ERROR org.springframework.boot.SpringApplication -- Application run failed
2025-11-24T01:54:23.607777569Z org.springframework.boot.context.config.InvalidConfigDataPropertyException: Property 'spring.profiles' imported from location 'class path resource [application.yml]' is invalid and should be replaced with 'spring.config.activate.on-profile' [origin: class path resource [application.yml] from app.jar - 4:12]
2025-11-24T01:54:23.607786609Z 	at org.springframework.boot.context.config.InvalidConfigDataPropertyException.lambda$throwIfPropertyFound$0(InvalidConfigDataPropertyException.java:113)
2025-11-24T01:54:23.607790819Z 	at java.base/java.util.LinkedHashMap.forEach(Unknown Source)
2025-11-24T01:54:23.607795309Z 	at java.base/java.util.Collections$UnmodifiableMap.forEach(Unknown Source)
2025-11-24T01:54:23.607798809Z 	at org.springframework.boot.context.config.InvalidConfigDataPropertyException.throwIfPropertyFound(InvalidConfigDataPropertyException.java:109)
2025-11-24T01:54:23.607802499Z 	at org.springframework.boot.context.config.ConfigDataEnvironment.checkForInvalidProperties(ConfigDataEnvironment.java:369)
2025-11-24T01:54:23.607804709Z 	at org.springframework.boot.context.config.ConfigDataEnvironment.applyToEnvironment(ConfigDataEnvironment.java:333)
2025-11-24T01:54:23.607806769Z 	at org.springframework.boot.context.config.ConfigDataEnvironment.processAndApply(ConfigDataEnvironment.java:238)
2025-11-24T01:54:23.60780896Z 	at org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor.postProcessEnvironment(ConfigDataEnvironmentPostProcessor.java:96)
2025-11-24T01:54:23.607811189Z 	at org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor.postProcessEnvironment(ConfigDataEnvironmentPostProcessor.java:89)
2025-11-24T01:54:23.607815069Z 	at org.springframework.boot.env.EnvironmentPostProcessorApplicationListener.onApplicationEnvironmentPreparedEvent(EnvironmentPostProcessorApplicationListener.java:132)
2025-11-24T01:54:23.60781722Z 	at org.springframework.boot.env.EnvironmentPostProcessorApplicationListener.onApplicationEvent(EnvironmentPostProcessorApplicationListener.java:115)
2025-11-24T01:54:23.60781954Z 	at org.springframework.context.event.SimpleApplicationEventMulticaster.doInvokeListener(SimpleApplicationEventMulticaster.java:185)
2025-11-24T01:54:23.60782172Z 	at org.springframework.context.event.SimpleApplicationEventMulticaster.invokeListener(SimpleApplicationEventMulticaster.java:178)
2025-11-24T01:54:23.6078238Z 	at org.springframework.context.event.SimpleApplicationEventMulticaster.multicastEvent(SimpleApplicationEventMulticaster.java:156)
2025-11-24T01:54:23.60782588Z 	at org.springframework.context.event.SimpleApplicationEventMulticaster.multicastEvent(SimpleApplicationEventMulticaster.java:138)
2025-11-24T01:54:23.60782799Z 	at org.springframework.boot.context.event.EventPublishingRunListener.multicastInitialEvent(EventPublishingRunListener.java:136)
2025-11-24T01:54:23.60783014Z 	at org.springframework.boot.context.event.EventPublishingRunListener.environmentPrepared(EventPublishingRunListener.java:81)
2025-11-24T01:54:23.60783223Z 	at org.springframework.boot.SpringApplicationRunListeners.lambda$environmentPrepared$2(SpringApplicationRunListeners.java:64)
2025-11-24T01:54:23.60783435Z 	at java.base/java.lang.Iterable.forEach(Unknown Source)
2025-11-24T01:54:23.60783646Z 	at org.springframework.boot.SpringApplicationRunListeners.doWithListeners(SpringApplicationRunListeners.java:118)
2025-11-24T01:54:23.60783851Z 	at org.springframework.boot.SpringApplicationRunListeners.doWithListeners(SpringApplicationRunListeners.java:112)
2025-11-24T01:54:23.60784058Z 	at org.springframework.boot.SpringApplicationRunListeners.environmentPrepared(SpringApplicationRunListeners.java:63)
2025-11-24T01:54:23.60784277Z 	at org.springframework.boot.SpringApplication.prepareEnvironment(SpringApplication.java:353)
2025-11-24T01:54:23.60785207Z 	at org.springframework.boot.SpringApplication.run(SpringApplication.java:313)
2025-11-24T01:54:23.60785436Z 	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1361)
2025-11-24T01:54:23.60785644Z 	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1350)
2025-11-24T01:54:23.60785876Z 	at com.recipemate.RecipeMateApplication.main(RecipeMateApplication.java:18)
2025-11-24T01:54:23.60786097Z 	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source)
2025-11-24T01:54:23.607863101Z 	at java.base/java.lang.reflect.Method.invoke(Unknown Source)
2025-11-24T01:54:23.60786525Z 	at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:106)
2025-11-24T01:54:23.60786733Z 	at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:64)
2025-11-24T01:54:23.607869441Z 	at org.springframework.boot.loader.launch.JarLauncher.main(JarLauncher.java:40)
2025-11-24T01:54:26.015862857Z ==> Exited with status 1
2025-11-24T01:54:26.191362696Z ==> Common ways to troubleshoot your deploy: https://render.com/docs/troubleshooting-deploys