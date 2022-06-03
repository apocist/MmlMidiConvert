package com.inverseinnovations.convertFrontend;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.inverseinnovations.mmlMidiConvert.*;

public class Frontend {
InputStream midiGlobal;
MmlMidiConvert midi;
volatile Thread thread;
	public Frontend(){
		JFrame frame = new JFrame("MML Convertor");
		frame.setSize(800,600);//client window size
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setVisible(true);
	    JPanel panel=new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	    JLabel title = new JLabel("Insert MML Here:");
	    JPanel inputPanel = new JPanel();
	    final JTextArea inputField=new JTextArea(10,60);inputField.setName("input");//inputField.setMaximumSize(new Dimension(20,10));
	    JScrollPane inputScroll = new JScrollPane(inputField);
	    inputPanel.add(inputScroll);inputPanel.setVisible(true);

	    JPanel butPanel=new JPanel();
	    butPanel.setLayout(new BoxLayout(butPanel, BoxLayout.X_AXIS));
	    JButton playBut=new JButton("Play");playBut.setName("play");playBut.setSize(new Dimension(20,10));
	    playBut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//stopSong();
	    		midi = new MmlMidiConvert();
	    		playSong(midi.parseMML(inputField.getText()));
			}
        });
	    JButton stopBut=new JButton("Stop");stopBut.setName("stop");stopBut.setSize(new Dimension(20,10));
	    stopBut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
	    		stopSong();
			}
        });
	    JButton saveBut=new JButton("Save to Midi");saveBut.setName("save");saveBut.setSize(new Dimension(30,10));
	    saveBut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				midi = new MmlMidiConvert();
	    		writeToFile(midi.parseMML(inputField.getText()));
			}
        });
	    butPanel.add(playBut);
	    butPanel.add(stopBut);
	    butPanel.add(saveBut);

	    frame.getContentPane().add(panel);
	    panel.add(title);
	    panel.add(inputPanel);
	    panel.add(butPanel);
	    panel.setVisible(true);
	    frame.validate();
	}

	void stopSong() {
        if (thread != null) {
            thread = null;
            try {
            	Thread.sleep(1001);
            }
            catch (InterruptedException ie) {}
        }
    }

	void playSong(InputStream midi) {
		midiGlobal = midi;
		if (thread != null){stopSong();}
		if (thread == null) {
            thread = new Thread(){
    	    	Sequence sequence;
    	    	Sequencer sequencer;
    			public void run() {
    				try {
    					sequence = MidiSystem.getSequence(midiGlobal);
    					sequencer = MidiSystem.getSequencer();
    				}
    	    		catch (Exception e1) {
    	    		}
    				Thread thisThread = Thread.currentThread();
    				while (thread == thisThread) {
    					try{
    						sequencer.open();
    						sequencer.setSequence(sequence);
    						sequencer.start();
    						while (sequencer.isRunning() && thread != null) {
    		                    try {
    		                        Thread.sleep(1000);//1000
    		                    } catch (InterruptedException e) { }
    		                }
    					}
    		    		catch (Exception e1) {
    		    			break;
    		    	    } finally{
    		    	    	if(sequencer.isOpen()){
	    						sequencer.stop();
	    						sequencer.close();
    		    	    	}
    		    	    }
    				}
    			}
    	    };

            thread.start();
        }
    }

	public void writeToFile(InputStream list){
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream("out.midi"));
			byte[] buffer = new byte[1024];
			int len;
			while ((len = list.read(buffer)) != -1) {
			    os.write(buffer, 0, len);
			}
			/*for (byte b : list) {
				   os.write(b);
			}*/
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Frontend main = new Frontend();
	}
}
