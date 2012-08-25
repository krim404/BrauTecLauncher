package com.kokakiwi.mclauncher;

import javax.swing.JOptionPane;

/*
import java.util.ArrayList;
*/
public class MCLauncher
{
	/*
    @SuppressWarnings("unused")
    private static final int MIN_HEAP         = 511;
    @SuppressWarnings("unused")
    private static final int RECOMMENDED_HEAP = 1024;
    */
	
    public static void main(String[] args) throws Exception
    {
    	
        final float heapSizeMegs = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
        if (heapSizeMegs < 900.0F)
        {
        	JOptionPane.showMessageDialog(null, "Your RAM is very low. Please give Java more RAM / Start with the BAT or close some applications", "Warning - can be laggy", JOptionPane.PLAIN_MESSAGE);
        }
        LauncherFrame.main(args);
        
        /*}
        else
        {
            try
            {
                final String pathToJar = MCLauncher.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI().getPath();
                
                final ArrayList<String> params = new ArrayList<String>();
                
                params.add("javaw");
                params.add("-Xmx1024m");
                params.add("-Dsun.java2d.noddraw=true");
                params.add("-Dsun.java2d.d3d=false");
                params.add("-Dsun.java2d.opengl=false");
                params.add("-Dsun.java2d.pmoffscreen=false");
                
                params.add("-classpath");
                params.add(pathToJar);
                params.add("com.kokakiwi.mclauncher.LauncherFrame");
                final ProcessBuilder pb = new ProcessBuilder(params);
                final Process process = pb.start();
                if (process == null)
                {
                    throw new Exception("!");
                }
                System.exit(0);
            }
            catch (final Exception e)
            {
                //e.printStackTrace();
                LauncherFrame.main(args);
            }
        }*/
    }
}
