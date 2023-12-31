# TrafficFlowOptimizer - moduł Backend

## Opis
Moduł jest częścią serwerową aplikacji.
Wszystkie inne komponenty komunikują się wyłącznie przez Backend.
Użytkownik nie wchodzi z modułem bezpośrednio w interakcję - używa do tego Frontendu.

## Wykorzystane zewnętrzne biblioteki
Moduł oparto na **frameworku Spring** i **języku Java**.\
Plik [pom.xml](pom.xml) deklaruje pozostałe biblioteki oraz ich wersje (dependencies). Użytkownik budując projekt nie musi przejmować się ich instalacją, gdyż Maven zrobi to za niego automatycznie.

## Jak uruchomić moduł

Backend można uruchomić na dwa sposoby.\
Prostszym sposobem jest wykorzystanie Dockera. Jednak w przypadku chęci włączenia modułu w trybie deweloperskim można to zrobić wykorzystując Mavena.

### Za pomocą Dockera

##### Wymagania:
* zainstalowany Docker
* zainstalowane narzędzie Docker Compose
* uruchomiona Baza Danych

##### Instrukcja:
* uruchomić Dockera
* w katalogu projektu: `docker compose up`

### Za pomocą Mavena

##### Wymagania:
* zainstalowana Java 17
* zainstalowany Maven
* uruchomiona Baza Danych

##### Instrukcja:
* katalog projektu należy otworzyć w ulubionym IDE (przykładowo IntelliJ Idea)
* wybudować projekt za pomocą Mavena
* uruchomić [aplikację](src/main/java/app/backend/BackendApplication.java) z konfiguracją używającą zmiennych z pliku [.env](.env) (w Idea można użyć do tego [plugin](https://plugins.jetbrains.com/plugin/7861-envfile)) 

## Dokumentacja

* [Dokumentacja Springa](https://docs.spring.io/spring-framework/reference/index.html)
* [Dokumentacja Testcontainers](https://java.testcontainers.org)
* [Dokumentacja Dockera](https://docs.docker.com)