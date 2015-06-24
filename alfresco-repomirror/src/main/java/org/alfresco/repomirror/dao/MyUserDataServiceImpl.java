package org.alfresco.repomirror.dao;

import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataServiceImpl;

import com.mongodb.DB;

/**
 * 
 * @author sglover
 *
 */
public class MyUserDataServiceImpl extends UserDataServiceImpl
{
	public MyUserDataServiceImpl(DB db, String collection)
    {
        super(db, collection);
    }

    @Override
    public UserData findUserByUsername(String username)
    {
    	UserData userData = null;
    	if(username.equals("admin"))
    	{
    		userData = new UserData();
    		userData.setUsername("admin");
    		userData.setPassword("admin");
    	}
    	else
    	{
    		userData = super.findUserByUsername(username);
    	}
    	return userData;
    }

}
