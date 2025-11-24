# Enigma Machine – End-to-End Java Project

This repository contains a full implementation of the Enigma encryption machine, developed as part of the *End-to-End Software Development – Enigma 2025* course.  
The project evolves across three stages, starting from a console application and ending with a complete Spring Boot server.

---

## 🚀 Project Structure

### **Exercise 1 – Console Application**
- Java 21 standalone console app  
- XML-based machine loading  
- Manual and automatic code configuration  
- Rotor stepping, reflectors, and message processing  
- History tracking and timing statistics  
- Clear separation between Engine and UI modules

### **Exercise 2 – Maven Modular System**
- Migration to a multi-module Maven architecture  
- Modules: `machine`, `engine`, `loader`, `console`, `aggregator-enigma`  
- Support for dynamic rotor count  
- Full plugboard (pair) implementation  
- Build via uber-JAR using Maven Assembly

### **Exercise 3 – Spring Boot Server**
- REST API for all machine capabilities  
- Endpoints for loading XML, managing configurations, processing input, and retrieving history  
- Postman collection support  
- Controllers + Services following clean layered design  
- Packaged as a deployable Spring Boot JAR

---

## 📦 Technologies
- **Java 21**
- **Maven**
- **Spring Boot**
- **JAXB (XML parsing)**
- **Postman (API testing)**

---

## 📁 Repository Layout
/machine        # Rotor, reflector, stepping logic
/engine         # Machine manager and operations
/loader         # XML loading and validation
/console        # Console UI (Exercise 1)
/app-server     # Spring Boot server (Exercise 3)
/aggregator-enigma  # Maven parent POM

---

## 📝 Running the Project
Each exercise includes its own runnable JAR and `run.bat` script as required by the course.  
For the full system (Exercise 3):

```bash
mvn clean install
java -jar app-server/target/enigma-machine-server-ex3.jar
