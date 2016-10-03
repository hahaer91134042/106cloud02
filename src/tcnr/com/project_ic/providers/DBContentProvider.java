package tcnr.com.project_ic.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;


public class DBContentProvider extends ContentProvider {
	private static final String AUTHORITY = "tcnr.com.project_ic.providers.DBContentProvider";
	public static final String DB_FILE = "icare.db", DB_TABLE_login = "icare_login", DB_TABLE_loginstatus="icare_loginstatus",
			                                         DB_TABLE_GCMmsg="icare_GCMmsg",DB_TABLE_OpenData="icare_OpenData";
	private static final int URI_ROOT = 0, DB_TABLE_FRIENDS = 1;
	public static final Uri CONTENT_URI_login = Uri.parse("content://" + AUTHORITY + "/" + DB_TABLE_login);
	public static final Uri CONTENT_URI_loginStatus = Uri.parse("content://" + AUTHORITY + "/" + DB_TABLE_loginstatus);
	public static final Uri CONTENT_URI_GCMmsg = Uri.parse("content://" + AUTHORITY + "/" + DB_TABLE_GCMmsg);
	public static final Uri CONTENT_URI_OpenData = Uri.parse("content://" + AUTHORITY + "/" + DB_TABLE_OpenData);
	private static final UriMatcher sUriMatcher = new UriMatcher(URI_ROOT);

	static {
		sUriMatcher.addURI(AUTHORITY, DB_TABLE_login, DB_TABLE_FRIENDS);
		sUriMatcher.addURI(AUTHORITY, DB_TABLE_loginstatus, DB_TABLE_FRIENDS);
		sUriMatcher.addURI(AUTHORITY, DB_TABLE_GCMmsg, DB_TABLE_FRIENDS);
		sUriMatcher.addURI(AUTHORITY, DB_TABLE_OpenData, DB_TABLE_FRIENDS);
	}
	private SQLiteDatabase mFriendDb;

	@Override
	public boolean onCreate() {
		// ---宣告 使用Class DbOpenHelper.java 作為處理SQLite介面
		// Content Provider 就是 data Server, 負責儲存及提供資料, 他允許任何不同的APP使用
		// 共同的資料(不同的APP用同一個SQLite).

		DBOpenHelper friendDbOpenHelper = new DBOpenHelper(getContext(), DB_FILE, null, 1);

		mFriendDb = friendDbOpenHelper.getWritableDatabase();
		// 檢查資料表是否已經存在，如果不存在，就建立一個。
		Cursor cursor = mFriendDb.rawQuery(
				"select DISTINCT tbl_name from sqlite_master where tbl_name = '" + DB_TABLE_loginstatus + "'", null);
		if (cursor != null) {
			if (cursor.getCount() == 0) { // 沒有loginstatus資料表，要建立一個資料表。
				//
				mFriendDb.execSQL("CREATE TABLE " + DB_TABLE_loginstatus + " (" + "id INTEGER PRIMARY KEY,"
						+ "loginDate DATETIME DEFAULT CURRENT_TIMESTAMP," + "account TEXT NOT NULL,"
						+ "code TEXT NOT NULL," + "loginStatus TEXT NOT NULL);");
			}
			cursor.close();
		}

		Cursor cursorlogin = mFriendDb.rawQuery(
				"select DISTINCT tbl_name from sqlite_master where tbl_name = '" + DB_TABLE_login + "'", null);
		if (cursorlogin != null) {
			if (cursorlogin.getCount() == 0) { // 沒有login資料表，要建立一個資料表。
				mFriendDb.execSQL("CREATE TABLE " + DB_TABLE_login + " (" + "id INTEGER PRIMARY KEY,"
						+ "account TEXT NOT NULL," + "code TEXT NOT NULL," + "phone TEXT NOT NULL,"
						+ "mail TEXT NOT NULL," + "reg_id TEXT NOT NULL," + "user_ip TEXT NOT NULL,"
						+ "account_status TEXT NOT NULL," + "account_lvl TEXT NOT NULL," + "date TEXT NOT NULL);");
			}
			cursorlogin.close();
		}

		Cursor cursorGcmMsg = mFriendDb.rawQuery(
				"select DISTINCT tbl_name from sqlite_master where tbl_name = '" + DB_TABLE_GCMmsg + "'", null);
		if (cursorGcmMsg != null) {
			if (cursorGcmMsg.getCount() == 0) { // 沒有GCMmsg資料表，要建立一個資料表。
				mFriendDb.execSQL("CREATE TABLE " + DB_TABLE_GCMmsg + " (" + "id INTEGER PRIMARY KEY,"
						+ "msgDate DATETIME DEFAULT CURRENT_TIMESTAMP," + "GCMmsg TEXT NOT NULL);");
			}
			cursorGcmMsg.close();
		}

		Cursor cursorOpenData = mFriendDb.rawQuery(
				"select DISTINCT tbl_name from sqlite_master where tbl_name = '" + DB_TABLE_OpenData + "'", null);
		if (cursorOpenData != null) {
			if (cursorOpenData.getCount() == 0) { // 沒有OpenData資料表，要建立一個資料表。
				mFriendDb.execSQL("CREATE TABLE " + DB_TABLE_OpenData + " (" + "id INTEGER PRIMARY KEY,"
						+ "weather TEXT NOT NULL," + "hospital TEXT NOT NULL," + "TaKaChia TEXT NOT NULL);");
			}
			cursorOpenData.close();
		}
		// mFriendDb.execSQL("CREATE TABLE "+DB_TABLE_loginstatus+" ("+"id
		// INTEGER PRIMARY KEY," +"loginDate DATETIME DEFAULT
		// CURRENT_TIMESTAMP,"
		// +"account TEXT NOT NULL,"+"code TEXT NOT NULL,"+"loginStatus TEXT NOT
		// NULL);");
		//
		// mFriendDb.execSQL("CREATE TABLE "+DB_TABLE_GCMmsg+" ("+"id INTEGER
		// PRIMARY KEY," +"msgDate DATETIME DEFAULT CURRENT_TIMESTAMP,"
		// +"GCMmsg TEXT NOT NULL);");
		// mFriendDb.execSQL("CREATE TABLE "+DB_TABLE_OpenData+" ("+"id INTEGER
		// PRIMARY KEY,"+"weather TEXT NOT NULL,"+"hospital TEXT NOT NULL,"
		// +"TaKaChia TEXT NOT NULL);");

		// Cursor cursor2 = mFriendDb
		// .rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name
		// = '" + DB_TABLE_loginstatus + "'", null);
		// if (cursor2 != null) {
		// if (cursor2.getCount() == 0) // 沒有資料表，要建立一個資料表。
		//
		// mFriendDb.execSQL("CREATE TABLE "+DB_TABLE_loginstatus+"
		// ("+"loginDate DATETIME DEFAULT CURRENT_TIMESTAMP,"
		// +"account TEXT NOT NULL,"+"loginStatus TEXT NOT NULL);");
		// //"loginDate DATETIME DEFAULT CURRENT_TIMESTAMP,"+"loginState TEXT
		// NOT NULL
		// cursor2.close();
		// }

		return true;
	}
//	Cursor DbList=mFriendDB.query(distinct=ture 重複的資料只取一個  false全show
//  , table=資料表名稱
//  , columns=欄位名稱
//  , selection  指定查詢條件
//  , selectionArgs  指定查尋條件 的參數
//  , groupBy  指定分組
//  , having  指定分組條件
//  , orderBy 指定排序條件
//  , limit   指定查詢結果顯示多少條紀錄
//  , cancellationSignal          )
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (sUriMatcher.match(uri) != DB_TABLE_FRIENDS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		Cursor c=null;  //判斷每張表格所使用的uri
		if(uri.equals(CONTENT_URI_login)){
//		Cursor c = mFriendDb.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
		 c = mFriendDb.query(true, DB_TABLE_login, projection, selection, selectionArgs,null, null, sortOrder, null ); //"ASC DESC"
		 c.setNotificationUri(getContext().getContentResolver(), uri);
		}else if(uri.equals(CONTENT_URI_loginStatus)){
		    c = mFriendDb.query(true, DB_TABLE_loginstatus, projection, selection, selectionArgs,null, null, sortOrder, null ); //"ASC DESC"
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}else if(uri.equals(CONTENT_URI_GCMmsg)){
		    c = mFriendDb.query(true, DB_TABLE_GCMmsg, projection, selection, selectionArgs,null, null, sortOrder, null ); //"ASC DESC"
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}else if(uri.equals(CONTENT_URI_OpenData)){
		    c = mFriendDb.query(true, DB_TABLE_OpenData, projection, selection, selectionArgs,null, null, sortOrder, null ); //"ASC DESC"
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		
		return c;
		
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		
		if (sUriMatcher.match(uri) != DB_TABLE_FRIENDS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	    if(uri.equals(CONTENT_URI_login)){
	    	long rowId = mFriendDb.insert(DB_TABLE_login, null, values);
			if (rowId > 0) {
				// 在已有的 Uri的後面追加ID數據
				Uri insertedRowUri = ContentUris.withAppendedId(CONTENT_URI_login, rowId);
				
				// 通知數據已經改變
				getContext().getContentResolver().notifyChange(insertedRowUri, null);
				return insertedRowUri;	
			}
	    }else if(uri.equals(CONTENT_URI_loginStatus)) {
	    	long rowId = mFriendDb.insert(DB_TABLE_loginstatus, null, values);
			if (rowId > 0) {
				// 在已有的 Uri的後面追加ID數據
				Uri insertedRowUri = ContentUris.withAppendedId(CONTENT_URI_loginStatus, rowId);
				
				// 通知數據已經改變
				getContext().getContentResolver().notifyChange(insertedRowUri, null);
				return insertedRowUri;	
			}
		}else if (uri.equals(CONTENT_URI_GCMmsg)) {
	    	long rowId = mFriendDb.insert(DB_TABLE_GCMmsg, null, values);
			if (rowId > 0) {
				// 在已有的 Uri的後面追加ID數據
				Uri insertedRowUri = ContentUris.withAppendedId(CONTENT_URI_GCMmsg, rowId);
				
				// 通知數據已經改變
				getContext().getContentResolver().notifyChange(insertedRowUri, null);
				return insertedRowUri;
			}
		}else if (uri.equals(CONTENT_URI_OpenData)) {
	    	long rowId = mFriendDb.insert(DB_TABLE_OpenData, null, values);
			if (rowId > 0) {
				// 在已有的 Uri的後面追加ID數據
				Uri insertedRowUri = ContentUris.withAppendedId(CONTENT_URI_OpenData, rowId);
				
				// 通知數據已經改變
				getContext().getContentResolver().notifyChange(insertedRowUri, null);
				return insertedRowUri;
			}
		}

		
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (sUriMatcher.match(uri) != DB_TABLE_FRIENDS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		if (uri.equals(CONTENT_URI_login)) {
		    int rowsAffected = mFriendDb.delete(DB_TABLE_login, selection, null);
			return rowsAffected;
		}else if (uri.equals(CONTENT_URI_loginStatus)) {
			 int rowsAffected = mFriendDb.delete(DB_TABLE_loginstatus, selection, null);
			 return rowsAffected;
		} else if (uri.equals(CONTENT_URI_GCMmsg)) {
			int rowsAffected = mFriendDb.delete(DB_TABLE_GCMmsg, selection, null);
			 return rowsAffected;
		} else if (uri.equals(CONTENT_URI_OpenData)) {
			int rowsAffected = mFriendDb.delete(DB_TABLE_OpenData, selection, null);
			 return rowsAffected;
		}else {		
			return -1;
		}
       
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (sUriMatcher.match(uri) != DB_TABLE_FRIENDS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if(uri.equals(CONTENT_URI_login)){
			int rowsAffected = mFriendDb.update(DB_TABLE_login, values, selection, null);
			return rowsAffected;
		}else if (uri.equals(CONTENT_URI_loginStatus)) {
			int rowsAffected = mFriendDb.update(DB_TABLE_loginstatus, values, selection, null);
			return rowsAffected;
		}else if (uri.equals(CONTENT_URI_OpenData)) {
			int rowsAffected = mFriendDb.update(DB_TABLE_OpenData, values, selection, null);
			return rowsAffected;
		}
		return -1;
	}


}
