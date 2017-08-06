package org.yourorghere;


import com.sun.opengl.util.Animator;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Random;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;




/**
 * Assig.java <BR>
 * author: Brian Paul (converted to Java by Ron Cemer and Sven Goethel) <P>
 *
 * This version is equal to Brian Paul's version 1.2 1999/10/21
 */
public class Assig implements GLEventListener,KeyListener {

    int time = 0;
    float eyeypos = 0.0f;
    float eyexpos = 25.0f;
    float eyezpos = 25.0f;
    float objectx = 10.0f;
    float objectz = 11.0f;
    float objecty = -20.0f;
    
    float fov = 30.0f;
    boolean isRefreshCamera = false;
    Texture brick;
    Texture floor;
    Texture tabletop;
    
    float tablex = 10.0f;
    float tablez = 10.0f;
    
    float spotx = 8.0f;
    float spot_d_x = -0.5f;
    float spota= 10.0f; 
    float playerx = 4.0f;
    float playerz = 0.0f;
    float CPUx = -3.5f;
    float CPUz = 0.0f;
    float puckx = 0.1f;
    float puckz = 0.0f;
    
    float puckr = 0.2f;
    float malletsr = 0.3f;
    
    boolean isGameOver = false;
    boolean isdarken = false;
    boolean isPlayerWin = false;
    boolean isMoving = false;
    boolean isHitPlayer = false;
    
    float destx = 13.0f ;
    float destz = -15.0f ;
    float cpuDestz = 2;
    int count = 0 ;
    long puckRunTime = 50;
    
    Clip backgroundclip;
    File  tableAudioFile =  new File("audio/cantotable1.wav");
    File malletAudioFile =  new File("audio/cantotable2.wav");
    File winAudioFile =  new File("audio/win.wav");
    File failAudioFile =  new File("audio/fail.wav");
    
    CPU cpu;
    Puck puck;
    int move = 0;
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Air Hockey");
        GLCanvas canvas = new GLCanvas();
        Assig mainobj = new Assig();
        canvas.addGLEventListener(mainobj);
        canvas.addKeyListener(mainobj);
        frame.add(canvas);
        frame.setSize(640, 480);
        final Animator animator = new Animator(canvas);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                // Run this on another thread than the AWT event queue to
                // make sure the call to Animator.stop() completes before
                // exiting
                new Thread(new Runnable() {

                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });
        // Center frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        canvas.requestFocus();
        animator.start();
    }

    public void init(GLAutoDrawable drawable) {
        // Use debug pipeline
        // drawable.setGL(new DebugGL(drawable.getGL()));

        GL gl = drawable.getGL();
        System.err.println("INIT GL IS: " + gl.getClass().getName());

        // Enable VSync
        gl.setSwapInterval(1);

        // Setup the drawing area and shading mode
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glShadeModel(GL.GL_SMOOTH); // try setting this to GL_FLAT and see what happens.
        gl.glEnable(GL.GL_DEPTH_TEST);
        
        cpu= new CPU();
        cpu.start();
        
        puck = new Puck();
        puck.start();
        
        playBackground();
        puckPosition();
        initLighting(drawable);
        initTexture(drawable); 
    }
    
    private void initLighting(GLAutoDrawable drawable) {
        float light_ambient[] =    { 20.0f, 20.0f, 20.0f, 1.0f };
        float light_diffuse[] =    { 1.0f, 1.0f, 1.0f, 1.0f };
        float light_position[] = { 1.0f, 50.0f, 1.0f, 0.0f };
        float light_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        GL gl = drawable.getGL();
        
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, light_ambient, 0);
        gl.glLightfv(GL.GL_LIGHT0,GL.GL_DIFFUSE, light_diffuse, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, light_specular, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light_position, 0);
      
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_LIGHT0);

         
        
     }
    
    
    private void initTexture(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        try { 
            // loading in the texture
        //text = TextureIO.newTexture(new File("earth.bmp"), false);      
        
        brick = TextureIO.newTexture(new File("image/brick.bmp"), false);
        
        floor = TextureIO.newTexture(new File("image/floor.bmp"), false);
        tabletop = TextureIO.newTexture(new File("image/tabletop.bmp"), false);
        
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        
      
        if (height <= 0) { // avoid a divide by zero error!
        
            height = 1;
        }
        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        setCamPerspective(gl,fov,h);
    }
   
    private void setCamPerspective(GL gl,float fovy, float aspect)
    {
        GLU glu = new GLU();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(fovy, aspect, 1.0, 100.0);
        
    }
    private void setTableMaterial(GL gl)
    {  
        float[] mat_ambient = {0.03f, 0.01f , 0.0f, 1.0f };
        float[] mat_diffuse = {0.6f, 0.2f , 0.0f, 1.0f };
        float[] mat_specular = {0.03f, 0.01f, 0.f, 1.0f };
        float[] mat_emission = {0.0f, 0.0f, 0.0f, 1.0f };
    
  
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, mat_ambient,0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, mat_diffuse,0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular,0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, mat_emission,0);
 

    }    
  
    private void setTopMaterial(GL gl)
    {  
        float[] mat_ambient = {0.1f, 0.1f , 0.1f, 1.0f };
        float[] mat_diffuse = {1.0f, 1.0f , 1.0f, 1.0f };
        float[] mat_specular = {0.03f, 0.0f, 0.0f, 1.0f };
        float[] mat_emission = {0.0f, 0.0f, 0.0f, 1.0f };
    
  
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, mat_ambient,0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, mat_diffuse,0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular,0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, mat_emission,0);
 

    }
    
    private void drawTable(GL gl)
    {        
        /*GLUquadric qobj = glu.gluNewQuadric();
        
        glu.gluSphere(qobj, 1.0, 40, 40);*/
       gl.glTranslatef(tablex, -8.5f, tablez);
        
        gl.glPushMatrix();
        gl.glTranslatef(3.0f, -8.5f, 3.0f);
        
        
        //gl.glColor3f(0.6f, 0.2f , 0.0f);
        gl.glScalef(10.0f, 5.0f, 5.0f);
        
       
     setTableMaterial(gl);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex3f( 0.5f, -0.5f, -0.5f );
        gl.glTexCoord2f(1, 0); gl.glVertex3f( -0.5f, -0.5f, -0.5f );
        gl.glTexCoord2f(1, 1); gl.glVertex3f( -0.5f, 0.5f, -0.5f );
        gl.glTexCoord2f(0, 1); gl.glVertex3f( 0.5f, 0.5f, -0.5f );
        gl.glEnd();
        
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex3f(0.5f, -0.5f, 0.5f );
        gl.glTexCoord2f(1, 0); gl.glVertex3f( 0.5f, -0.5f, -0.5f );
        gl.glTexCoord2f(1, 1); gl.glVertex3f( 0.5f, 0.5f, -0.5f );
        gl.glTexCoord2f(0, 1); gl.glVertex3f( 0.5f, 0.5f, 0.5f );
        gl.glEnd();
        
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex3f( -0.5f, -0.5f, 0.5f );
        gl.glTexCoord2f(1, 0); gl.glVertex3f( 0.5f, -0.5f, 0.5f );
        gl.glTexCoord2f(1, 1); gl.glVertex3f( 0.5f, 0.5f, 0.5f );
        gl.glTexCoord2f(0, 1); gl.glVertex3f( -0.5f, 0.5f, 0.5f );
        gl.glEnd();
        
        
        
        gl.glBegin(GL.GL_QUADS); 
        gl.glTexCoord2f(0, 0); gl.glVertex3f( -0.5f, -0.5f, -0.5f );
        gl.glTexCoord2f(1, 0); gl.glVertex3f( -0.5f, -0.5f, 0.5f );
        gl.glTexCoord2f(1, 1); gl.glVertex3f( -0.5f, 0.5f, 0.5f );
        gl.glTexCoord2f(0, 1); gl.glVertex3f( -0.5f, 0.5f, -0.5f );
        gl.glEnd();
     
        setTopMaterial(gl);
        int count = 25;
        float d = 0.5f / count;
        int zcount = 25;
        float zd = 0.5f/zcount;
       tabletop.enable();
        tabletop.bind(); 
        int xir = -count;
        int x=0;
        int z=0;
        for(int zix = -zcount+1; zix < count+1; zix++){
            for(int xix = -count+1; xix<count+1; xix++){
                
                gl.glBegin(GL.GL_QUADS);
               // gl.glNormal3f(0.0f, 0.0f, 1.0f);
                gl.glTexCoord2f(0+0.02f*x, 0.02f*z); 
                gl.glVertex3f(1.0f * d * (xix - 1), 0.5f, 1.0f * d * (zix - 1.0f));
                
                gl.glTexCoord2f(0+0.02f*x, 0+0.02f*z);
                gl.glVertex3f(1.0f * d * (xix-1),0.5f,1.0f * d * (zix) );
                
                gl.glTexCoord2f(0.02f*x, 0+0.02f*z); 
                gl.glVertex3f(1.0f * d * (xix),  0.5f,1.0f * d * (zix));
                
                gl.glTexCoord2f(0.02f*x, 0.02f*z); 
                gl.glVertex3f(1.0f * d * (xix ), 0.5f,1.0f * d * (zix-1.0f) );
                gl.glEnd();
              z++; 
            } 
             z=0;
             x++;
           
        } 
        tabletop.disable();
        gl.glPopMatrix();
        
    }
    
    private void setPuckMaterial(GL gl)
    {
        float[] mat_ambient = { 0.01f, 0.01f, 0.01f, 1.0f };
        float[] mat_diffuse = {0.5f, 0.5f, 0.5f, 1.0f };
         float[] mat_specular = {0.1f, 0.1f, 0.1f, 1.0f };
         float[] mat_emission = {0.0f, 0.0f, 0.0f, 1.0f };
      
  
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, mat_ambient,0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, mat_diffuse,0);
         gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular,0);
         gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, mat_emission,0);
        gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 50.0f);
       

    }
    
    
    private void drawPuck(GL gl, GLUT glut )
    {
        gl.glPushMatrix();
        setPuckMaterial(gl);
       
                  
        gl.glTranslatef(puckx, -5.8f, puckz);  
        gl.glRotated(90 ,1.0,0.0,0.0); 
        gl.glTranslatef(3.0f, 3.0f, 0.0f);  
       glut.glutSolidCylinder(puckr,0.25,10,5);
       

        gl.glPopMatrix();
   
    }
    private void setPlayerMallet(GL gl){
     float[] mat_ambient = { 0.0f, 0.0f, 0.01f, 1.0f };
        float[] mat_diffuse = {0.0f, 0.0f, 0.5f, 1.0f };
         float[] mat_specular = {0.0f, 0.0f, 1.0f, 1.0f };
         float[] mat_emission = {0.0f, 0.0f, 0.0f, 1.0f };
      
  
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, mat_ambient,0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, mat_diffuse,0);
         gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular,0);
         gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, mat_emission,0);
        gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 50.0f);
    }
    
    private void drawPlayerMallet(GL gl, GLUT glut){
      gl.glPushMatrix();
        setPlayerMallet(gl);
       
                  
        gl.glTranslatef(playerx, -5.8f, playerz);  
        gl.glRotated(90 ,1.0,0.0,0.0); 
        gl.glTranslatef(3.0f, 3.0f, 0.0f);  
       glut.glutSolidCylinder(malletsr,0.35,15,6);
       

        gl.glPopMatrix();

    }
    
     private void setCPUMallet(GL gl){
     float[] mat_ambient = { 0.01f, 0.0f, 0.0f, 1.0f };
        float[] mat_diffuse = {0.5f, 0.0f, 0.0f, 1.0f };
         float[] mat_specular = {1.0f, 0.0f, 0.0f, 1.0f };
         float[] mat_emission = {0.0f, 0.0f, 0.0f, 1.0f };
      
  
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, mat_ambient,0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, mat_diffuse,0);
         gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular,0);
         gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, mat_emission,0);
        gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 50.0f);
    }
    
    private void drawCPUMallet(GL gl, GLUT glut){
      gl.glPushMatrix();
        setCPUMallet(gl);
       
                  
        gl.glTranslatef(CPUx, -5.8f, CPUz);  
        gl.glRotated(90 ,1.0,0.0,0.0); 
        gl.glTranslatef(3.0f, 3.0f, 0.0f);  
       glut.glutSolidCylinder(malletsr,0.35,15,6);
       

        gl.glPopMatrix();
    }
   
    private void setboxMaterial(GL gl)
    {
          float[] mat_ambient = { 0.1f, 0.1f, 0.1f, 1.0f };
        float[] mat_diffuse = {0.1f, 0.1f, 0.1f, 1.0f };
         float[] mat_specular = {0.1f, 0.1f, 0.1f, 1.0f };
         float[] mat_emission = {0.0f, 0.0f, 0.0f, 1.0f };
      
  
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, mat_ambient,0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, mat_diffuse,0);
         gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular,0);
         gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, mat_emission,0);
        gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 50.0f);
    }
    
    
     private void drawStarBox(GL gl)
     {  
        gl.glPushMatrix();
        gl.glScalef(100.0f, 100.0f, 100.0f);
        
        setboxMaterial(gl);
         
        int count = 25;
        float d = 0.5f / count;
       floor.enable();
        floor.bind(); 
        int xir = -count;
        int x=0;
        int z=0;
        for(int zix = -count; zix < count; zix++){
            for(int xix = -count; xix<count; xix++){
                
                gl.glBegin(GL.GL_QUADS);
                gl.glNormal3f(0.0f, 0.0f, 1.0f);
                gl.glTexCoord2f(0+0.02f*z, 0+0.02f*x); gl.glVertex3f(1.0f * d * (xix - 1), -0.5f, 1.0f * d * (zix - 1));
                gl.glTexCoord2f(0+0.02f*z, 0.02f*x); gl.glVertex3f(1.0f * d * (xix-1),-0.5f,1.0f * d * (zix) );
                gl.glTexCoord2f(0.02f*z, 0.02f*x); gl.glVertex3f(1.0f * d * (xix),  -0.5f,1.0f * d * (zix));
                gl.glTexCoord2f(0.02f*z, 0+0.02f*x); gl.glVertex3f(1.0f * d * (xix ), -0.5f,1.0f * d * (zix-1) );
                gl.glEnd();
              x++; 
            } 
             x=0;
               z++;
           
        } 
        floor.disable();
        
        gl.glPopMatrix();
       
         
         
     }
     
     public void spotLigh(GL gl){
        gl.glPushMatrix();
        float light_ambient[] = { 50.5f, 50.5f, 0.0f, 1.0f };
        float light_diffuse[] = { 100.0f, 100.0f, 0.0f, 1.0f };
        float light_specular[] = { 1.0f, 1.0f, 0.0f, 1.0f }; 
        
        
        float[] light_position = { spotx, 0.5f, 3.0f, 1.0f };
        float[] light_dir = {spot_d_x, -5.0f, 0.0f, 0.0f };
   
          
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, light_ambient, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, light_diffuse, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, light_specular, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, light_position, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPOT_DIRECTION, light_dir, 0);
        
        gl.glLightf(GL.GL_LIGHT1,GL.GL_SPOT_CUTOFF, spota);
        
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_LIGHT1);
        gl.glPopMatrix();
        
     }
     
    public void display(GLAutoDrawable drawable) {
         
        
        GL gl = drawable.getGL();
        GLU glu = new GLU();
        GLUT glut = new GLUT();
        if (isRefreshCamera)
        {
            setCamPerspective(gl,fov,640.0f/480.0f);
            isRefreshCamera = false;
        }
        gl.glMatrixMode(GL.GL_MODELVIEW);
        // Clear the drawing area
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        // Reset the current matrix to the "identity"
        
        
        gl.glLoadIdentity();
        
        
        
        
        glu.gluLookAt(eyexpos, eyeypos,eyezpos,  objectx, objecty, objectz,   0.0, 1.0, 0.0);
        
        drawStarBox(gl);
        drawTable(gl); 
        
        
       
        
        drawPuck(gl,glut);
        drawPlayerMallet(gl,glut);
        drawCPUMallet(gl,glut);
        if(isdarken){
            gl.glDisable(GL.GL_LIGHTING);
            gl.glDisable(GL.GL_LIGHT0);
            spotLigh(gl);
           
        }else{
            gl.glDisable(GL.GL_LIGHTING);
            gl.glDisable(GL.GL_LIGHT1);
            initLighting(drawable); 
        }
 
        // Flush all drawing operations to the graphics card
        gl.glFlush();
        
        time++;
        time = time % 100;
 

            try{

                Thread.sleep(puckRunTime);
            }catch(Exception e){}
        
    }
    
    // handle key input
    public void keyTyped(KeyEvent ke){}
    
    public void keyPressed(KeyEvent ke)
    {
        
    }
   public void keyReleased(KeyEvent ke)
    {
        switch(ke.getKeyCode())
        {
           case KeyEvent.VK_LEFT:
                
                //
                if(playerz+0.5<=2){
                    playerz+=0.5;
                    isMoving = true;
                }
                
                //teaz+=0.5;
                break;
            
            case KeyEvent.VK_RIGHT:
               
                //
                if(playerz-0.5>=-2.0){
                    playerz-=0.5;
                    isMoving = true;
                }
                
               //teaz-=0.5;
                break;
                
            case KeyEvent.VK_ADD:
                // zoom in 
                /*fov -= 2.0;
                 if (fov < 0.0f)
                    fov = 0.0f;
                 isRefreshCamera = true;
              isdarken=false;*/
                spotx+=0.5;
                break;
                
              case KeyEvent.VK_MINUS:
                // zoom out 
                /* fov += 2.0;
              if (fov > 70.0f)
                    fov = 70.0f;
                isRefreshCamera = true;
                isdarken=true;*/
                spotx-=0.5;
                
                break;
                
              case KeyEvent.VK_A:
                  eyexpos=25.0f;
                  eyezpos=25.0f;
                objectx = 10.0f;
                objectz = 11.0f;
                objecty = -20.0f;
                tablez = 10.0f;
                  break;
               
              case KeyEvent.VK_D:
                  eyexpos=25.0f;
                  eyezpos=-18.0f;
                objectx = 5.0f;
                objectz = 5.0f;
                objecty = -33.0f;
                tablez = -10.0f;
                  break;
            
            
        }
        
        
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }
    
    public void checkCollision(){
        float distance =(float)(0.2+0.3);
        float mp_r = malletsr+puckr+0.025f;
        float cpuCpuck_x = CPUx-puckx;
        float cpuCpuck_z = CPUz-puckz;
        float cp_direction = (float)(Math.sqrt(cpuCpuck_x*cpuCpuck_x+cpuCpuck_z*cpuCpuck_z));
        
        float playerCpuck_x = playerx-puckx;
        float playerCpuck_z = playerz-puckz;
        float pp_direction = (float)(Math.sqrt(playerCpuck_x*playerCpuck_x+playerCpuck_z*playerCpuck_z));    
        
        if(cp_direction<mp_r){
            playAudio(malletAudioFile);
            if(puckx<CPUx){
                destx = puckx-CPUx-20;
            }
            else{
                destx=puckx-CPUx+20;
            }
            if(puckz>CPUz)
                 destz=puckz-CPUz+20;
            else
               destz=puckz-CPUz-20;
                
            isHitPlayer = false;
            changeForce();
        }else if(pp_direction<mp_r){
            playAudio(malletAudioFile);
            
            if(puckx<playerx){
                    destx=puckx-playerx-20;
                }
                else{
                    destx=puckx-playerx+20;
                }
            
            if(puckz>playerz)
                 destz=puckz-playerz+20;
            else
               destz=puckz-playerz-20;  
            
            isHitPlayer = true;
            changeForce();
        }
        
        
        
    }
    
    public void movePuck(){
    float distance = 1.0f;
      if(!checkGameOver()){
        if(destx>0.1){
            if(puckx+0.1f<=4.6f){
                puckx+=0.1f;
                puckx = (float)(Math.round(puckx*100))/100;
            }else{
               playAudio(tableAudioFile);
               isHitPlayer = true;
                destx = -destx;
                changeForce();
            }
        }else{
            if(puckx-0.1f>=-4.4f){
                puckx-=0.1;
                puckx = (float)(Math.round(puckx*100))/100;
            }else{
                playAudio(tableAudioFile);
                destx=-destx;
            }
        }
        
        
        
        if(destz>0.0){
            if(puckz+0.1<=2.2){
                puckz+=0.1;
                puckz = (float)(Math.round(puckz*100))/100;
            }else{
                 playAudio(tableAudioFile);
                destz=-destz;
                
            }
        }else{
             if(puckz-0.1>=-2.2){
                 
                puckz-=0.1;
                puckz = (float)(Math.round(puckz*100))/100;
            }else{
              playAudio(tableAudioFile);
                destz=-destz;
            }
        }
      }else{
      finalMoving();
      }
    }
    
    public void finalMoving(){
        if(count<=8){
         if(destx>0.1){
                puckx+=0.1f;
                puckx = (float)(Math.round(puckx*100))/100;
            
        }else{
                puckx-=0.1;
                puckx = (float)(Math.round(puckx*100))/100; 
        }   
        if(destz>0.0){
                puckz+=0.1;
                puckz = (float)(Math.round(puckz*100))/100;
            
        }else{
                puckz-=0.1;
                puckz = (float)(Math.round(puckz*100))/100;
            
        }
        count++;
        }else{
            isdarken=true;
            stopBackgroundMusic();
            if(count==9){
                if(isPlayerWin){
                    playAudio(winAudioFile);
                }else{
                    playAudio(failAudioFile);
                }
                count++;
            new java.util.Timer().schedule( 
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            restart();
                            isdarken = false;
                        }
                    }, 
                    5000 
            );    
                 
        }
        //isdarken=true;
        }
    }
    
    public boolean checkGameOver(){
        if(count==0){
        if(puckx+0.1>4.6f){
            if(puckz<=0.75&&puckz>=-0.5){
                spotx =8.0f;
                spot_d_x= -0.5f;
                isPlayerWin = false;
                return true;
            }
        }else if(puckx-0.1<-4.4f){
            if(puckz<=0.75&&puckz>=-0.5){
                spotx = -8.0f;
                spot_d_x=  5.0f;
                isPlayerWin = true;       
                return true;
            }
        }
        return false;
        
        }
    return true;
    }
    

    public void puckPosition(){
    Random random = new Random();   
     if(random.nextInt(2)==1){
        destz = 50;
    }else{
        destz = -50;
    }
    }
    
    public void changeForce(){
        float playerForce = 30.0f;
        float puckForce = (float)(0.5*10)/(puckRunTime/10);
        
        if(isHitPlayer){
            if(isMoving){
                if(destx>playerx&&puckx<playerx){
                 puckForce = -puckForce; 
                }
                float x = playerx - puckx;
                float z = playerz - puckz;
                float h = (float)Math.sqrt(x*x+z*z);
                z = Math.abs(z);
                x = Math.abs(x);
                float angle = (float)Math.toDegrees(Math.atan2(x,z));
                if(angle>=90)angle=70;
                puckRunTime = (int)Math.abs(( 0.5 / (playerForce*Math.cos(angle)-puckForce))*1000);   
                
            }
        }else{
            float x = CPUx - puckx;
            float z = CPUz - puckz;
            float h = (float)Math.sqrt(x*x+z*z);
            z = Math.abs(z);
            x = Math.abs(x);
            float angle = (float)Math.toDegrees(Math.atan2(x,z));
            if(angle>=90)angle=70;
            puckRunTime = (int)Math.abs(( 0.5 / (playerForce*Math.cos(angle)-puckForce))*1000);     
            
        }
        
    }
    
    public void restart(){
        isGameOver = false;
        isdarken = false;
        isPlayerWin = false;
        isMoving = false;
        isHitPlayer = false;
        
        count = 0;
        puckRunTime = 50;
        
        
        puckx = 0.1f;
        puckz = 0.0f;
        
        destx = 20;
        puckPosition();
       
        playBackground();
        System.out.print("Destx: "+ destx + "\nDestz: "+ destz+"\n");
    }
    
    public class CPU extends Thread{
        public void run(){
            while(true){
                if(!isdarken)
                    moveCPU();
                time++;
                time = time % 100;
                try{

                    Thread.sleep(100);
                }catch(Exception e){}
            }
        }
        
         public void moveCPU(){
                if(cpuDestz==2){
                    if(CPUz+0.5<=2){

                         CPUz+=0.5;
                    }else{

                        cpuDestz = -cpuDestz;
                    }
                }else{
                    if(CPUz-0.5>=-2){

                        CPUz-=0.5;
                    }else{

                        cpuDestz = -cpuDestz;
                    }
                }
        }
    }
    
    public class Puck extends Thread{
        public void run(){
             while(true){
                if(!isdarken)
                time++;
                time = time % 100;
               try{
                            checkCollision();
                            movePuck();
                            isMoving = false;
                             if(move==15){
                                puckRunTime +=1.0;
                                move=0;
                             }else
                                move++;
                            if(puckRunTime>60){
                                puckRunTime = 60;
                            }
                            System.out.print("puckRunTime "+(long)puckRunTime+"\n");
                            Thread.sleep(puckRunTime);
                        }catch(Exception e){}
            }
           }
    }
    
    
    
        private void playAudio(File file){
       try {
        Clip clip = AudioSystem.getClip();
        clip.open(AudioSystem.getAudioInputStream(file));
        clip.start();
        } catch(Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    } 
        
        private void playBackground (){
       try {
        backgroundclip = AudioSystem.getClip();
        backgroundclip.open(AudioSystem.getAudioInputStream(new File("audio/bluebird.wav")));
        backgroundclip.loop(Clip.LOOP_CONTINUOUSLY);
        backgroundclip.start();
        } catch(Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    } 
      private void stopBackgroundMusic(){
          backgroundclip.stop();
      }  
  
}


