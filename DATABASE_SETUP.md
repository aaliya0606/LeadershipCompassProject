# PostgreSQL Database Setup Guide

## Leadership Compass Project

This guide explains how to install PostgreSQL, configure the local database, and connect it to the Leadership Compass backend application.



# 1. Install PostgreSQL

Download PostgreSQL:

* PostgreSQL Downloads: [https://www.postgresql.org/download/](https://www.postgresql.org/download/)

Recommended version:

* PostgreSQL 17

During installation:

* Install pgAdmin when prompted
* Remember the password you create for the `postgres` user 
* Note: password will be stored in plaintext
* Keep the default port:

```text
5432
```
* No extra items are needed in the install process


# 2. Open pgAdmin

After installation:

1. Open pgAdmin
2. Connect to the PostgreSQL server
3. Enter the password created during installation

You should see:

```text
Servers
→ PostgreSQL
```


# 3. Create the Database

In pgAdmin:

1. Right click:

```text
Databases
```

2. Click:

```text
Create → Database
```

3. Database name:

```text
leadership_compass
```

4. Owner:

```text
postgres
```

5. Save


# 4. Configure the Backend

Open:

```text
backend/src/main/resources/application.properties
```

In the datasource configuration:

```properties

spring.datasource.url=jdbc:postgresql://localhost:5432/leadership_compass
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD

```

Replace:

```text
YOUR_POSTGRES_PASSWORD
```

with the password created during PostgreSQL installation.


# 5. Run the Backend

Start the Spring Boot backend.

If successful:

* PostgreSQL tables will automatically be created
* Hibernate will generate tables from the entity classes

Check in pgAdmin:

```text
Databases
→ leadership_compass
→ Schemas
→ public
→ Tables
```

You should see tables such as:

```text
users
survey_questions
survey_responses
survey_results
```

# 8. Running the Application

Correct startup order:

```text
1. Ensure PostgreSQL is running
2. Start Spring Boot backend
3. Open frontend using Live Server
```

# 10. Adding Default Survey Questions

Run SQL in pgAdmin, using the Query Tool:

```sql
INSERT INTO survey_questions (question_text, leadership_area)
VALUES
('I proactively schedule regular one-on-one meetings with each team member and protect that time from interruptions or cancellations.', 'Caring Time'),
('When I spend time with team members, I am fully present-avoiding distractions like phones, email, or multitasking.', 'Caring Time'),
('I adjust the frequency and depth of Caring Time based on individual team members’ preferences and needs.', 'Caring Time'),
('I am available and attentive during moments when a team member is visibly stressed or in crisis, offering my undivided attention and support.', 'Caring Time'),
('I prioritise Caring Time even during busy periods, managing my calendar and energy to consistently make time for my team.', 'Caring Time'),
('I come to scheduled Caring Time prepared with meaningful agendas or topics that encourage open, supportive conversations beyond just status updates.', 'Caring Time'),
('I balance Caring Time with empowering team members’ independence to avoid micromanagement or dependency.', 'Caring Time'),
('I regularly use direct, face-to-face or video interactions for Caring Time rather than solely relying on email or quick check-ins.', 'Caring Time'),
('I consciously protect Caring Time boundaries by limiting distractions and communicating availability clearly to my team and colleagues.', 'Caring Time'),
('Over the past month, I have made consistent efforts to reach at least 6 hours of quality Caring Time per week for my team as research recommends.', 'Caring Time'),
('I give my team members my full, undistracted attention during conversations.','Receiving Value'),
('I listen with the intent to understand-not just to respond or reply.','Receiving Value'),
('I ask open-ended and clarifying questions to deepen my understanding of team members’ perspectives.','Receiving Value'),
('I take notes during or immediately after conversations to capture key points and show respect.','Receiving Value'),
('I follow up with the team to close the communication loop on their ideas, feedback, or concerns.','Receiving Value'),
('I create safe spaces where team members feel comfortable sharing honest thoughts without fear of judgment.','Receiving Value'),
('I demonstrate genuine curiosity and a learning mindset during interactions with my team.','Receiving Value'),
('I actively recognise and validate the contributions and ideas shared by team members in team settings.','Receiving Value'),
('I schedule regular, protected one-on-one meetings specifically dedicated to listening and understanding team members.','Receiving Value'),
('I model vulnerability by sharing my own learning and invite two-way dialogue to build trust.','Receiving Value'),
('I proactively identify and remove obstacles that hinder my team’s performance.','Acts of Support'),
('I ensure my team has the right tools, technologies, and resources to succeed.','Acts of Support'),
('I provide clear role definitions, responsibilities, and expectations for every team member.','Acts of Support'),
('I give timely, constructive feedback focused on behaviours and growth-not blame.','Acts of Support'),
('I collaborate with team members to set actionable next steps for continuous improvement.','Acts of Support'),
('I invest in my team’s development by connecting them with training, mentorship, and growth opportunities.','Acts of Support'),
('I protect my team from unnecessary distractions, conflicting priorities, and organisational politics.','Acts of Support'),
('I balance support with empowering team members to take ownership and solve problems independently.','Acts of Support'),
('I regularly check in with my team to identify blockers and respond quickly with practical solutions.','Acts of Support'),
('I create a culture of continuous support and learning by modelling vulnerability and openness as a leader.','Acts of Support'),
('I deliver recognition that is specific and clearly tied to concrete actions or results rather than generic praise.','Words of Recognition'),
('I focus recognition on contributions that exceed expectations, rather than routine or baseline performance.','Words of Recognition'),
('I give timely recognition, aiming to acknowledge efforts as close to the event as possible to maximise impact.','Words of Recognition'),
('I communicate the purpose and impact of the contribution clearly, helping team members understand why their effort matters.','Words of Recognition'),
('I use a variety of recognition methods, including verbal praise, written thank-you notes, peer recognition, and public shout-outs.','Words of Recognition'),
('I encourage and model peer-to-peer recognition within my team to build a culture of mutual appreciation.','Words of Recognition'),
('I tailor my recognition approach to individual preferences and generational differences, balancing public and private forms.','Words of Recognition'),
('I prepare ahead by noting meaningful contributions throughout projects so I can recognise them promptly and accurately.','Words of Recognition'),
('I resist giving empty, excessive, or insincere praise that could dilute the impact and credibility of my recognition.','Words of Recognition'),
('I recognise leaders above me as well as those I lead, understanding that upward recognition fosters alignment and motivation.','Words of Recognition'),
('I foster an environment where team members feel safe to speak up, share ideas, and admit mistakes without fear of judgment or retaliation.','Psychological Touch'),
('I demonstrate emotional courage by admitting my own uncertainties, limitations, and mistakes to model vulnerability.','Psychological Touch'),
('I communicate openly and transparently, sharing information honestly and inviting dialogue rather than issuing directives.','Psychological Touch'),
('I actively listen with empathy, practicing genuine presence and reflection to understand others’ feelings and perspectives.','Psychological Touch'),
('I treat mistakes as learning opportunities and encourage a no-blame culture focused on growth and development.','Psychological Touch'),
('I create clear, consistent expectations and promote shared responsibility, empowering the team to own challenges and solutions safely.','Psychological Touch'),
('I adjust my communication style and support to match the unique personalities, needs, and contexts of individual team members (e.g., using DISC profiling).','Psychological Touch'),
('I maintain calmness and emotional control, responding thoughtfully in tense situations to foster a steady, trusting team climate.','Psychological Touch'),
('I invest in developing my emotional intelligence-regularly reflecting on my emotions, triggers, and social skills to improve leadership presence.','Psychological Touch'),
('I proactively protect my team’s psychological well-being by recognising signs of stress or burnout and facilitating support or resources when needed.','Psychological Touch');
```

# 9. Resetting the Database (Optional)

If tables become inconsistent during development:

Open Query Tool in pgAdmin and run:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

Then restart the backend.
 
Tables will be recreated automatically.
