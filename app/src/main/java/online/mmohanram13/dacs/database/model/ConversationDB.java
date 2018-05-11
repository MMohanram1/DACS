package online.mmohanram13.dacs.database.model;

/**
 * Created by Mohan Ram M on 5/10/2018.
 */

public class ConversationDB {

    public static final String TABLE_NAME = "dacs_converse_data";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_RECIPIENT = "recipient";
    public static final String COLUMN_DISPLAYED = "viewed";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private int id;
    private String sender;
    private String recipient;
    private int viewed;
    private String data;
    private String timestamp;

    public ConversationDB(){}

    public ConversationDB(int id, String sender, String recipient, int viewed, String data, String timestamp){
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.viewed = viewed;
        this.data = data;
        this.timestamp = timestamp;
    }

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_SENDER + " TEXT,"
                    + COLUMN_RECIPIENT + " TEXT,"
                    + COLUMN_DISPLAYED + " INTEGER,"
                    + COLUMN_DATA + " TEXT,"
                    + COLUMN_TIMESTAMP + " TEXT"
                    + ")";

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getViewed() {
        return viewed;
    }

    public void setViewed(int viewed) {
        this.viewed = viewed;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
