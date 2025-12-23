# 📊 Painel do Autor - Dashboard Editora

Bem-vindo ao repositório do **Painel do Autor**. Este projeto é uma plataforma completa para gestão de autores, métricas, pagamentos e entregas de uma editora, composta por um backend robusto em Spring Boot e um frontend moderno em Angular.

## 📁 Estrutura do Projeto

O projeto está dividido em dois diretórios principais:

- **`backend/`**: API RESTful desenvolvida em **Java (Spring Boot)**.
- **`frontend/`**: Aplicação Web desenvolvida em **Angular**.

---

## 🚀 Backend (API)

O backend é responsável por toda a lógica de negócios, autenticação, integração com bancos de dados de e-commerce externos e envio de emails.

### Tecnologias Principais

- **Java 24**
- **Spring Boot 3.4.10**
- **Spring Security** (JWT Auth)
- **PostgreSQL 18** (Banco de dados)
- **Flyway** (Migrações de banco)
- **Swagger/OpenAPI** (Documentação da API)

### Como Rodar o Backend

1. Navegue até a pasta `backend`:
   ```bash
   cd backend
   ```
2. Configure as variáveis de ambiente necessárias (banco de dados, email, etc.) no seu ambiente de desenvolvimento ou IDE.
3. Execute a aplicação usando Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
   _Ou execute a classe principal `PainelDoAuthorBackendApplication.java` na sua IDE._

Para mais detalhes, consulte o [README do Backend](backend/README.md).

---

## 🎨 Frontend (Web)

O frontend é o painel administrativo e do autor, oferecendo interfaces ricas para visualização de gráficos, gestão de pedidos e suporte.

### Tecnologias Principais

- **Angular 20.3**
- **TailwindCSS** (Estilização)
- **ECharts** (Visualização de dados)
- **TypeScript**

### Como Rodar o Frontend

1. Navegue até a pasta `frontend`:
   ```bash
   cd frontend
   ```
2. Instale as dependências (necessário Node.js instalado):
   ```bash
   npm install
   ```
3. Inicie o servidor de desenvolvimento:
   ```bash
   npm start
   ```
4. Acesse a aplicação em: `http://localhost:4200`

---

## ⚙️ Pré-requisitos Gerais

Para executar todo o ecossistema localmente, você precisará de:

- **Java JDK 24**
- **Node.js** (LTS recomendado)
- **PostgreSQL** rodando localmente (ou acesso a um banco remoto)

## 🤝 Contribuição e Licença

Este projeto é privado e proprietário da **Andescore Software**.

---

**Desenvolvido com ❤️ pela equipe Andescore Software.**
