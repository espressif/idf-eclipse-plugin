package com.espressif.idf.core.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.espressif.idf.core.logging.Logger;
import com.google.gson.Gson;

public class IndexerDbOps
{
	private static final String CREATE_FUNCTIONS_TABLE_SQL = "CREATE TABLE function_tbl (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,body TEXT,file_definition TEXT, file_headers TEXT,description_openai TEXT)"; //$NON-NLS-1$
	private static final String CREATE_GPT_DESC_TABLE_SQL = "CREATE TABLE \"function_desc_gpt\" (\"id\" INTEGER, \"gpd_desc\" TEXT, \"func_ref\" INTEGER NOT NULL, PRIMARY KEY(\"id\" AUTOINCREMENT))"; //$NON-NLS-1$
	private static final String DB_NAME = "code_db.db"; //$NON-NLS-1$
	private static final String CONN_STRING = "jdbc:sqlite:"; //$NON-NLS-1$
	private static final String INSERT_FUNCS_TABLE_QUERY = "INSERT INTO function_tbl(name, body, file_definition, file_headers, description_openai) VALUES(?, ?, ?, ?, ?)"; //$NON-NLS-1$
	private static final String RECORD_PRESENCE_QUERY = "SELECT id FROM function_tbl WHERE name = ? AND body = ? AND file_definition = ? AND file_headers = ?"; //$NON-NLS-1$
	private static final String FETCH_ALL_QUERY = "SELECT * FROM function_tbl"; //$NON-NLS-1$
	private static final String INSERT_GPT_DESC_TABLE_QUERY = "INSERT INTO function_desc_gpt(gpd_desc, func_ref) VALUES (?, ?)"; //$NON-NLS-1$
	private static final String WHERE_ID_QUERY_CLAUSE = " WHERE id = ?";
	
	private static final String ID_COL = "id"; //$NON-NLS-1$
	private static final String FUNCTION_NAME_COL = "name"; //$NON-NLS-1$
	private static final String FUNCTION_BODY_COL = "body"; //$NON-NLS-1$
	private static final String FILE_DEFINITION_COL = "file_definition"; //$NON-NLS-1$
	private static final String FILE_HEADER_COL = "file_headers"; //$NON-NLS-1$
	private static final String DESCRIPTION_OPENAI_COL = "description_openai"; //$NON-NLS-1$

	private Connection connection;
	private PreparedStatement preparedStatement;

	private static IndexerDbOps indexerInsert;

	public static IndexerDbOps getIndexerDbOps()
	{
		if (indexerInsert == null)
			indexerInsert = new IndexerDbOps();
		return indexerInsert;
	}

	private IndexerDbOps()
	{
	}
	
	public IndexerVO fetchIndexerVo(int id) throws ClassNotFoundException, SQLException
	{
		checkAndOpenConnection();
		preparedStatement = connection.prepareStatement(FETCH_ALL_QUERY + WHERE_ID_QUERY_CLAUSE);
		preparedStatement.setInt(1, id);
		ResultSet resultSet = preparedStatement.executeQuery();
		Gson gson = new Gson();
		while (resultSet.next())
		{
			IndexerVO indexerVO = new IndexerVO();
			indexerVO.setId(resultSet.getInt(ID_COL));
			indexerVO.setFunctionName(resultSet.getString(FUNCTION_NAME_COL));
			indexerVO.setBody(resultSet.getString(FUNCTION_BODY_COL));
			indexerVO.setFileDefinition(resultSet.getString(FILE_DEFINITION_COL));
			indexerVO.setFileHeaders(resultSet.getString(FILE_HEADER_COL));
			indexerVO.setDescription(resultSet.getString(DESCRIPTION_OPENAI_COL));
			
			GptResponseVO gptResponseVO = gson.fromJson(indexerVO.getDescription(), GptResponseVO.class);
			indexerVO.setGptResponseVO(gptResponseVO);
			indexerVO.setGptDescription(gptResponseVO.getChoices().get(0).getMessage().getContent());
			return indexerVO;
		}
		return null;
	}

	public void insertGptFuncDesc(List<IndexerVO> indexerVOs) throws ClassNotFoundException, SQLException
	{
		checkAndOpenConnection();
		if (!tableExists(connection, "function_desc_gpt")) //$NON-NLS-1$
		{
			createTable(CREATE_GPT_DESC_TABLE_SQL);
		}
		
		preparedStatement = connection.prepareStatement(INSERT_GPT_DESC_TABLE_QUERY);
		connection.setAutoCommit(false);
		
		for (IndexerVO indexerVO : indexerVOs)
		{
			preparedStatement.setString(1, indexerVO.getGptDescription());
			preparedStatement.setInt(2, indexerVO.getId());
			preparedStatement.addBatch();
		}
		
		int [] affectedRows = preparedStatement.executeBatch();
		connection.commit();
		Logger.log("Inserted: " + affectedRows.length); //$NON-NLS-1$
	}
	
	public void insertFunctionDetail(IndexerVO indexerVO) throws ClassNotFoundException, SQLException
	{
		String functionName = indexerVO.getFunctionName();
		String body = indexerVO.getBody();
		String fileDefinition = indexerVO.getFileDefinition();
		String fileHeaders = indexerVO.getFileHeaders();
		String description = indexerVO.getDescription();
		insertFunctionDetail(functionName, body, fileDefinition, fileHeaders, description);
	}

	public boolean recordExists(IndexerVO indexerVO) throws ClassNotFoundException, SQLException
	{
		checkAndOpenConnection();
		if (!tableExists(connection, "function_tbl")) //$NON-NLS-1$
		{
			return false;
		}

		preparedStatement = connection.prepareStatement(RECORD_PRESENCE_QUERY);
		preparedStatement.setString(1, indexerVO.getFunctionName());
		preparedStatement.setString(2, indexerVO.getBody());
		preparedStatement.setString(3, indexerVO.getFileDefinition());
		preparedStatement.setString(4, indexerVO.getFileHeaders());
		ResultSet rs = preparedStatement.executeQuery();
		return rs.next();
	}

	public void insertFunctionDetail(String functionName, String body, String fileDefinition, String fileHeaders,
			String description) throws SQLException, ClassNotFoundException
	{
		checkAndOpenConnection();
		try
		{
			// Check if the table exists, and if not, create it
			if (!tableExists(connection, "function_tbl")) //$NON-NLS-1$
			{
				createTable(CREATE_FUNCTIONS_TABLE_SQL);
			}
			preparedStatement = connection.prepareStatement(INSERT_FUNCS_TABLE_QUERY);
			preparedStatement.setString(1, functionName);
			preparedStatement.setString(2, body);
			preparedStatement.setString(3, fileDefinition);
			preparedStatement.setString(4, fileHeaders);
			preparedStatement.setString(5, description);

			preparedStatement.executeUpdate();
		} finally
		{
			if (preparedStatement != null)
				preparedStatement.close();
			if (connection != null)
				connection.close();
		}
	}

	public List<IndexerVO> fetchIndexerVOs() throws ClassNotFoundException, SQLException
	{
		checkAndOpenConnection();
		List<IndexerVO> indexerVOs = new LinkedList<>();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(FETCH_ALL_QUERY);
		Gson gson = new Gson();
		while (resultSet.next())
		{
			IndexerVO indexerVO = new IndexerVO();
			indexerVO.setId(resultSet.getInt(ID_COL));
			indexerVO.setFunctionName(resultSet.getString(FUNCTION_NAME_COL));
			indexerVO.setBody(resultSet.getString(FUNCTION_BODY_COL));
			indexerVO.setFileDefinition(resultSet.getString(FILE_DEFINITION_COL));
			indexerVO.setFileHeaders(resultSet.getString(FILE_HEADER_COL));
			indexerVO.setDescription(resultSet.getString(DESCRIPTION_OPENAI_COL));

			GptResponseVO gptResponseVO = gson.fromJson(indexerVO.getDescription(), GptResponseVO.class);
			indexerVO.setGptResponseVO(gptResponseVO);
			indexerVO.setGptDescription(gptResponseVO.getChoices().get(0).getMessage().getContent());
			indexerVOs.add(indexerVO);
		}

		resultSet.close();
		statement.close();
		return indexerVOs;
	}

	private void checkAndOpenConnection() throws ClassNotFoundException, SQLException
	{
		Class.forName("org.sqlite.JDBC"); //$NON-NLS-1$
		String homeDir = System.getProperty("user.home"); //$NON-NLS-1$
		String connString = CONN_STRING + homeDir + File.separator + DB_NAME;
		if (connection == null || connection.isClosed())
		{
			connection = DriverManager.getConnection(connString);
		}
	}

	private void createTable(String tableCreationQuery) throws SQLException
	{
		preparedStatement = connection.prepareStatement(tableCreationQuery);
		preparedStatement.executeUpdate();
		preparedStatement.close();
	}

	private boolean tableExists(Connection conn, String tableName) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet res = meta.getTables(null, null, tableName, new String[] { "TABLE" }); //$NON-NLS-1$
		while (res.next())
		{
			if (res.getString("TABLE_NAME").equalsIgnoreCase(tableName)) //$NON-NLS-1$
			{
				return true;
			}
		}
		return false;
	}

}
