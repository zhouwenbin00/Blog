package com.test.utils;

import com.test.SerivceEnviroment;
import com.test.utils.sql.SQL;
import com.test.utils.sql.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/** DB工具类 @Auther: zhouwenbin @Date: 2019/5/17 16:47 */
public class DBUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(DBUtil.class);
  private static String driver = "com.mysql.jdbc.Driver";
  private static String url;
  private static String usr;
  private static String pwd;

  private DBUtil() {}

  static {
    /** 驱动注册 */
    try {
      Class.forName(driver);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    SerivceEnviroment serivceEnviroment = SerivceEnviroment.getSerivceEnviroment();
    url = serivceEnviroment.getDbUrl();
    usr = serivceEnviroment.getDbUsr();
    pwd = serivceEnviroment.getDbPwd();
  }

  /**
   * 通过url, user, password获取 Connetion
   *
   * @param url
   * @param user
   * @param password
   * @return
   * @throws SQLException
   */
  public static Connection getConnection(String url, String user, String password)
      throws SQLException {
    return DriverManager.getConnection(url, user, password);
  }

  /**
   * 获取 Connetion
   *
   * @return
   * @throws SQLException
   */
  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, usr, pwd);
  }

  /**
   * 执行update
   *
   * @param sql
   * @param params
   * @return
   */
  public static int executeUpdate(String sql, Object... params) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    int i = -1;
    try {
      connection = getConnection();
      preparedStatement = setSqlParams(connection, sql, params);
      i = preparedStatement.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
    } finally {
      close(connection, preparedStatement, null);
    }
    return i;
  }

  /**
   * 执行update
   *
   * @param update
   * @return
   */
  public static int executeUpdate(SQL update) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    int i = -1;
    try {
      connection = getConnection();
      preparedStatement = setSqlParams(connection, update.sql(), update.params());
      i = preparedStatement.executeUpdate();
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
    } finally {
      close(connection, preparedStatement, null);
      LOGGER.info("执行:{}参数：{}", update.sql(), Arrays.toString(update.params()));
    }
    return i;
  }

  /**
   * 执行查询，返回bean集合
   *
   * @param sql
   * @param clazz
   * @param params
   * @param <T>
   * @return
   */
  public static <T> List<T> QueryList(String sql, Class<T> clazz, Object... params) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    List<T> list = null;
    try {
      connection = getConnection();
      preparedStatement = setSqlParams(connection, sql, params);
      resultSet = preparedStatement.executeQuery();
      list = BeanUtil.newListFromResultSet(resultSet, clazz);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
    } finally {
      close(connection, preparedStatement, resultSet);
    }

    return list;
  }

  public static <T> List<T> QueryList(Select select, Class<T> clazz) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    List<T> list = null;
    try {
      connection = getConnection();
      preparedStatement = setSqlParams(connection, select.sql(), select.params());
      resultSet = preparedStatement.executeQuery();
      list = BeanUtil.newListFromResultSet(resultSet, clazz);
    } catch (SQLException e) {
      LOGGER.error(e.getMessage());
    } finally {
      close(connection, preparedStatement, resultSet);
      LOGGER.info("执行:{}参数：{}", select.sql(), Arrays.toString(select.params()));
    }

    return list;
  }

  /**
   * 执行查询，返回bean对象
   *
   * @param sql
   * @param clazz
   * @param <T>
   * @return
   */
  public static <T> T QueryOne(String sql, Class<T> clazz, Object... params) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    T t = null;
    try {
      connection = getConnection();
      preparedStatement = setSqlParams(connection, sql, params);
      resultSet = preparedStatement.executeQuery();
      t = BeanUtil.newObjFromResultSet(resultSet, clazz);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      close(connection, preparedStatement, resultSet);
      LOGGER.info("执行:{}参数：{}", sql, Arrays.toString(params));
    }
    return t;
  }

  public static <T> T QueryOne(Select select, Class<T> clazz) throws SQLException {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    T t = null;
    connection = getConnection();
    preparedStatement = setSqlParams(connection, select.sql(), select.params());
    resultSet = preparedStatement.executeQuery();
    t = BeanUtil.newObjFromResultSet(resultSet, clazz);
    close(connection, preparedStatement, resultSet);
    LOGGER.info("执行:{}参数：{}", select.sql(), Arrays.toString(select.params()));
    return t;
  }

  /**
   * 设置sql参数
   *
   * @param connection
   * @param sql
   * @param params
   * @return
   */
  private static PreparedStatement setSqlParams(
      Connection connection, String sql, Object... params) {
    PreparedStatement preparedStatement = null;
    try {
      preparedStatement = connection.prepareStatement(sql);
      if (params.length > 0) {
        for (int i = 0; i < params.length; i++) {
          preparedStatement.setObject(i + 1, params[i]);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return preparedStatement;
  }

  /**
   * 关闭连接
   *
   * @param connection
   * @param statement
   * @param rs
   */
  private static void close(Connection connection, Statement statement, ResultSet rs) {
    close(rs);
    close(statement);
    close(connection);
  }

  private static void close(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private static void close(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private static void close(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
