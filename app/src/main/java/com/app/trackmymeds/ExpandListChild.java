package com.app.trackmymeds;

import org.json.JSONObject;

/**
 * Created by Declan on 23/06/2017.
 */

public class ExpandListChild
{

	public String getName()
	{
		return m_name;
	}

	public void setName(String _name)
	{
		m_name = _name;
	}

	public String getTag()
	{
		return m_tag;
	}

	public void setTag(String _tag)
	{
		m_tag = _tag;
	}

	public JSONObject getJSON()
	{
		return m_json;
	}

	public void setJSON(JSONObject _json)
	{
		m_json = _json;
	}

	//Properties.
	private String m_name;
	private String m_tag;
	private JSONObject m_json;
}
