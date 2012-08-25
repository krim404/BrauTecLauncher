package com.kokakiwi.mclauncher.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.kokakiwi.mclauncher.utils.java.DownloadThread;
import com.kokakiwi.mclauncher.utils.java.Utils;

public class ProfileManager
{
    private final Map<String, Profile> profiles          = new HashMap<String, Profile>();
    private String                     currentProfile    = "default";
    private final String               profilesParentDir = Utils.getWorkingDirectory("bdh", null, false).getAbsolutePath() + "/"; //On règle le launcher pour qu'il aille chercher les profils
    
    public ProfileManager()
    {
    	final File versionFile = new File(profilesParentDir
                + "profiles/version");
    	String ProfilesVersion = "0";
    	String curVersion = "0";
    	
    	try {
    		ProfilesVersion = Utils.executePost("http://update.brautec.de/profiles", "", "");
	    	
	    	if(versionFile.exists())
	    	{
				curVersion = readVersion(versionFile);
	    	} 
    	} catch (Exception e) {

		}
    	if(ProfilesVersion != null && !ProfilesVersion.equalsIgnoreCase(curVersion))
    	{
    		System.out.println("Updating profiles");
    		final File target = new File(profilesParentDir, "profiles.zip");
    		try 
    		{
    			final DownloadThread thread = new DownloadThread(new URL("http://update.brautec.de/profiles.zip"),target);
				thread.start();
                while (!thread.isDownloaded())
                {
                	System.out.print(".");
                }
                
				extractZip(target,profilesParentDir);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		
            
    		//Update Profile
    		try {
				writeVersion(versionFile,ProfilesVersion);
			} catch (Exception e) {
			}
    	}
    	
        MCLogger.debug("Load profiles");
        MCLogger.info(profilesParentDir);
        final File defaultProfileDir = new File(profilesParentDir
                + "profiles/default");
        if (!defaultProfileDir.exists())
        {
            MCLogger.debug("Create default profile");
            defaultProfileDir.mkdirs();
            createProfile("Default");
        }
        
        /*
        //Kopiere Profile aus Hauptordner
        String path = "";
        path = this.getClass().getResource("/").getPath();
        
        MCLogger.debug("Looking for additional configs at: "+path);
        File f = new File(path+"/config.yml");
        if(f.exists())
        {
        	File f2 = new File(path+"/profile.yml");
        	if(f2.exists())
        	{
        		final Configuration descriptor = new Configuration();
                descriptor.load(f2);
                String id = descriptor.getString("id");
                System.out.println("Kopiere Profil '"+id+"' in Profile Dir");
                File uf = new File(profilesParentDir + "profiles/"+id);
                uf.mkdirs();
                moveFile(profilesParentDir + "profiles/"+id+"/profile.yml",f2);
                moveFile(profilesParentDir + "profiles/"+id+"/config.yml",f);	
        	}
        }
        */
        
        final File profilesDir = new File(profilesParentDir + "profiles");
        final String[] profDir = profilesDir.list();
        for (final String dir : profDir)
        {
            final File profileDir = new File(profilesDir, dir);
            if (profileDir.isDirectory())
            {
            	MCLogger.info("Load profile with ID '" + dir + "'");
                final File descriptorFile = new File(profileDir, "profile.yml");
                if (descriptorFile.exists())
                {
                    final Configuration descriptor = new Configuration();
                    descriptor.load(descriptorFile);
                    final Profile profile = new Profile(descriptor);
                    profiles.put(profile.getID(), profile);
                }
            }
        }
    }
    
    public static boolean moveFile(String target, File source) {
        File temp = new File(target);
 
        if(temp.exists())
            temp.delete();
        
        return source.renameTo(temp);
    }
    
    @SuppressWarnings("unchecked")
    private void extractZip(File file, String path) throws Exception
    {
        final ZipFile zipFile = new ZipFile(file);
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile
                .entries();
        
        while (entries.hasMoreElements())
        {
            final ZipEntry entry = entries.nextElement();
            if(entry.isDirectory())
            {
                continue;
            }
        }
        entries = (Enumeration<ZipEntry>) zipFile.entries();
        
        while (entries.hasMoreElements())
        {
            final ZipEntry entry = entries.nextElement();
            if(entry.isDirectory())
            {
                File dir = new File(path + entry.getName());
                if(!dir.exists())
                {
                    dir.mkdirs();
                }
                continue;
            }
            
            final File f = new File(path + entry.getName());
            if (f.exists() && !f.delete())
            {
                continue;
            }
            
            final InputStream in = zipFile.getInputStream(zipFile
                    .getEntry(entry.getName()));
            final OutputStream out = new FileOutputStream(new File(path
                    + entry.getName()));
            
            final byte[] buffer = new byte[65536];
            int bufferSize;
            while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1)
            {
                out.write(buffer, 0, bufferSize);
            }
            
            in.close();
            out.close();
        }
        
        zipFile.close();
    }
    
    public String readVersion(File file) throws Exception
    {
        final DataInputStream dis = new DataInputStream(new FileInputStream(
                file));
        final String mod = dis.readUTF();
        dis.close();
        return mod;
    }
    
    public void writeVersion(File file, String version) throws Exception
    {
        if (!file.exists())
        {
            file.createNewFile();
        }
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(
                file));
        dos.writeUTF(version);
        dos.close();
    }
    
    public void createProfile(String name)
    {
        final Profile profile = new Profile(name);
        profiles.put(profile.getID(), profile);
        try
        {
            profile.save();
        }
        catch (final Exception e)
        {
            MCLogger.error(e.getLocalizedMessage());
        }
    }
    
    public void deleteProfile(Profile profile)
    {
        deleteProfile(profile.getID());
    }
    
    public void deleteProfile(String id)
    {
        final File profileDir = new File(profilesParentDir + "profiles/" + id);
        deleteDirectory(profileDir);
        profiles.remove(id);
    }
    
    public void deleteDirectory(File dir)
    {
        if (dir.isDirectory())
        {
            for (final String sub : dir.list())
            {
                final File subElement = new File(dir, sub);
                if (subElement.isDirectory())
                {
                    deleteDirectory(subElement);
                }
                subElement.delete();
            }
        }
        dir.delete();
    }
    
    public Profile getCurrentProfile()
    {
        return getProfile(currentProfile);
    }
    
    public void setCurrentProfile(Profile profile)
    {
        setCurrentProfile(profile.getID());
    }
    
    public void setCurrentProfile(String id)
    {
        currentProfile = id;
    }
    
    public Profile getProfile(String id)
    {
        return profiles.get(id);
    }
    
    public Map<String, Profile> getProfiles()
    {
        return profiles;
    }
    
    public class Profile
    {
        private String              name;
        private String              id;
        private Configuration       descriptor = new Configuration();
        private final Configuration config     = new Configuration();
        
        /**
         * Load a profile from a descriptor file
         * 
         * @param descriptor
         *            Descriptor file
         */
        public Profile(Configuration descriptor)
        {
            name = descriptor.getString("name");
            id = descriptor.getString("id");
            this.descriptor = descriptor;
            loadConfig();
        }
        
        /**
         * Create a new profile
         * 
         * @param name
         *            Profile name
         */
        public Profile(String name)
        {
            this.name = name;
            id = name.toLowerCase().trim();
            loadConfig();
            final File descFile = new File(profilesParentDir + "profiles/" + id
                    + "/profile.yml");
            if (!descFile.exists())
            {
                try
                {
                    descFile.getParentFile().mkdirs();
                    descFile.createNewFile();
                    initDescriptor();
                    descriptor.save(descFile);
                }
                catch (final Exception e)
                {
                    MCLogger.info(e.getLocalizedMessage());
                }
            }
        }
        
        public void loadConfig()
        {
            config.load(Utils.getResourceAsStream("config/config.yml"), "yaml");
            final File confFile = new File(profilesParentDir + "profiles/" + id
                    + "/config.yml");
            if (!confFile.exists())
            {
                try
                {
                    confFile.getParentFile().mkdirs();
                    confFile.createNewFile();
                }
                catch (final IOException e)
                {
                    MCLogger.info(e.getLocalizedMessage());
                }
            }
            config.load(confFile);
        }
        
        public void initDescriptor()
        {
            descriptor.set("name", name);
            descriptor.set("id", id);
        }
        
        public void save() throws Exception
        {
            final File confFile = new File(profilesParentDir + "profiles/" + id
                    + "/config.yml");
            config.save(confFile);
            saveDescriptor();
        }
        
        public void saveDescriptor() throws Exception
        {
            final File descFile = new File(profilesParentDir + "profiles/" + id
                    + "/profile.yml");
            descriptor.save(descFile);
        }
        
        public String getName()
        {
            return name;
        }
        
        public String getID()
        {
            return id;
        }
        
        public void setName(String name)
        {
            this.name = name;
            descriptor.set("name", name);
        }
        
        public void setID(String id)
        {
            this.id = id;
            descriptor.set("id", id);
        }
        
        public Configuration getDescriptor()
        {
            return descriptor;
        }
        
        public Configuration getConfig()
        {
            return config;
        }
        
        @Override
        public String toString()
        {
        	if(name.equalsIgnoreCase("Default"))
        		return "BrauTec";
        	else
        		return name;
        }
    }
}
