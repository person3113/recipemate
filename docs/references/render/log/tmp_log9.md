- 회원가입하려고 하니까 internal server error 뜨는데.

  at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:281)
  Position: 232] [insert into users (comment_notification,created_at,deleted_at,email,group_purchase_notification,manner_temperature,nickname,password,phone_number,points,profile_image_url,role,updated_at) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]; SQL [insert into users (comment_notification,created_at,deleted_at,email,group_purchase_notification,manner_temperature,nickname,password,phone_number,points,profile_image_url,role,updated_at) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]
  Hint: You will need to rewrite or cast the expression.
  org.springframework.dao.InvalidDataAccessResourceUsageException: could not execute statement [ERROR: column "role" is of type user_role but expression is of type character varying
  Position: 232] [insert into users (comment_notification,created_at,deleted_at,email,group_purchase_notification,manner_temperature,nickname,password,phone_number,points,profile_image_url,role,updated_at) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]; SQL [insert into users (comment_notification,created_at,deleted_at,email,group_purchase_notification,manner_temperature,nickname,password,phone_number,points,profile_image_url,role,updated_at) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]
  Hint: You will need to rewrite or cast the expression.
  Unexpected error: could not execute statement [ERROR: column "role" is of type user_role but expression is of type character varying
  Position: 232
  Hint: You will need to rewrite or cast the expression.
  2025-11-25T04:12:16.575Z ERROR 1 --- [RecipeMate] [nio-8080-exec-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: column "role" is of type user_role but expression is of type character varying
  2025-11-25T04:12:16.575Z  WARN 1 --- [RecipeMate] [nio-8080-exec-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42804

  Position: 232] [insert into users (comment_notification,created_at,deleted_at,email,group_purchase_notification,manner_temperature,nickname,password,phone_number,points,profile_image_url,role,updated_at) values (?,?,?,?,?,?,?,?,?,?,?,?,?)]
  Hint: You will need to rewrite or cast the expression.
  Caused by: org.hibernate.exception.SQLGrammarException: could not execute statement [ERROR: column "role" is of type user_role but expression is of type character varying
  Position: 232
  Hint: You will need to rewrite or cast the expression.
  Caused by: org.postgresql.util.PSQLException: ERROR: column "role" is of type user_role but expression is of type character varying