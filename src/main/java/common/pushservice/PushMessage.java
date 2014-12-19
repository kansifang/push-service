package common.pushservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PushMessage {
	public String alert;
    public HashMap<String,String> extra = new HashMap<String,String>();
    public List<String> recipients = new ArrayList<String>();
    public String sound;
}
