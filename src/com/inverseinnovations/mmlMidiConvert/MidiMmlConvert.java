package com.inverseinnovations.mmlMidiConvert;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import javax.sound.midi.*;


public class MidiMmlConvert {
	public static final int NOTE_ON = 0x90;
	public static final int NOTE_OFF = 0x80;
	public static final int POLYPHONIC_KEY = 0xA0;
	public static final int CONTROL_CHANGE = 0xB0;//7=volume/10=pan//64 = Damper Pedal
	public static final int INSTRUMENT = 0xC0;
	public static final int CHANNEL_PRESSURE = 0xC0;
	public static final int PITCH_WHEEL = 0xE0;
	public static final String[] NOTE_NAMES = {"c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "a#", "b"};
	public static final String[] NOTE_NAMES_UPPER = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

	public static String parseMidi() throws InvalidMidiDataException, IOException{
		//System.out.println(new File("test.midi").getAbsolutePath());
		Sequence sequence = MidiSystem.getSequence(new File("test.midi"));
		Playlist playlist = new Playlist();

		int trackNumber = 0;
		for (Track track :  sequence.getTracks()) {

			MTrack cTrack = new MTrack(trackNumber);
			trackNumber++;
			System.out.println("Track " + trackNumber + ": size = " + track.size());
			System.out.println();

			for (int i=0; i < track.size(); i++) {
				MidiEvent event = track.get(i);
				long timestamp = event.getTick();
				//System.out.print("@" + timestamp + " ");
				MidiMessage message = event.getMessage();
				int command;
				if (message instanceof ShortMessage) {
					ShortMessage sm = (ShortMessage) message;
					//System.out.print("Channel: " + sm.getChannel() + " ");
					command = sm.getCommand();
					if (command == NOTE_ON) {
						if(sm.getData2() != 0){createNoteOn(sm, timestamp, cTrack);}
						else{createNoteOff(sm, timestamp, cTrack);}
					}
					else if (command == NOTE_OFF) {
						createNoteOff(sm, timestamp, cTrack);
					}
					else if (command == POLYPHONIC_KEY) {
						System.out.println("@"+timestamp+" Polyphonic Key: Key="+sm.getData1()+" Value="+sm.getData2());
						cTrack.addCmd(new PolyphonicKey(sm.getData1(), sm.getData2()));
					}
					else if (command == CONTROL_CHANGE) {
						System.out.println("@"+timestamp+" Control Change: Control="+sm.getData1()+" Value="+sm.getData2());
						cTrack.addCmd(new ControllerChange(sm.getData1(), sm.getData2()));
					}
					else if (command == INSTRUMENT) {
						System.out.println("@"+timestamp+" Instrument="+sm.getData1());
					}
					else if (command == CHANNEL_PRESSURE) {
						System.out.println("@"+timestamp+" Channel Pressure: Value="+sm.getData1());
						cTrack.addCmd(new ChannelPressure(sm.getData1(), sm.getData2()));
					}
					else if (command == PITCH_WHEEL) {
						System.out.println("@"+timestamp+" Pitch Wheel: l="+sm.getData1()+" m="+sm.getData2());
						cTrack.addCmd(new PitchWheel(sm.getData1(), sm.getData2()));
					}
					else {
						System.out.println("Command:" + sm.getCommand());
					}
				}
				else if(message instanceof MetaMessage){
					MetaMessage mm = (MetaMessage) message;
					if(mm.getType() == 81){
						//int bytes = fromByteArray(mm.getData());
						int bytes = new BigInteger(mm.getData()).intValue();
						//System.out.println("Tempo = "+(mm.getData()[0] & 0XFF)+(mm.getData()[1] & 0XFF)+(mm.getData()[2] & 0XFF));
						System.out.println("Tempo = "+bytes);
						//cTrack.addCmd(new Tempo(Integer.parseInt(""+(mm.getData()[0] & 0XFF)+(mm.getData()[1] & 0XFF)+(mm.getData()[2] & 0XFF))));
						cTrack.addCmd(new Tempo(bytes));
						}
					else if(mm.getType() == 47){System.out.println("End of Track");}
					else{System.out.println("MetaMsg "+mm.getType()+" Status= "+mm.getStatus());}
				}
				else if(message instanceof SysexMessage){
					SysexMessage sys = (SysexMessage) message;
					System.out.println("@"+timestamp+" SysexMsg "+sys.getStatus());
				}
				else {
					System.out.println("@"+timestamp+" Other message: " + message.getClass());
				}
			}

			System.out.println();
			playlist.addTrack(cTrack);
		}

		String[] tracks = new String[15];
		for(int i=0; i<playlist.getSize(); i++){
			//System.out.println("Track "+i+":");
			tracks[i] = playlist.getTrack(i).compile();
			//System.out.println(tracks[i]);
			//System.out.println();
		}
		//System.out.println();
		String theReturn = "";
		for(int i=0; i<tracks.length; i++){
			if(tracks[i] != null){
				if(tracks[i] != ""){
					//System.out.println(tracks[i]+",");
					theReturn += tracks[i]+",";
				}
			}
		}
		return theReturn;

	}

	private static void createNoteOff(ShortMessage sm, long timestamp, MTrack track) {
		int key = sm.getData1();
		int note = key % 12;
		int octave = (key / 12)-1;
		int savedKey;

		System.out.print("Note off "+NOTE_NAMES_UPPER[note]+octave+" ");
		Note cmd = track.getLastNote();
		//Note test;
		if(cmd != null){
			if((savedKey = cmd.getKey()) == key){
				if(!cmd.getReleased()){
					cmd.setLength(timestamp - cmd.getTimestamp());
					cmd.setReleased(true);
					System.out.println("@"+timestamp+" Note OFF, "+NOTE_NAMES[cmd.getNote()]+cmd.getOctave()+" velocity:"+cmd.getVelocity()+"(MML "+Math.round(cmd.getVelocity()/ 8.47)+") duration: "+cmd.getLength());
				}
				/*else{
					System.out.println(""+key+" was already relased");
					//track.addCmd(new Note(sm.getChannel(), key, note, octave, sm.getData2(), timestamp));
					test = new Note(sm.getChannel(), key, note, octave, sm.getData2(), cmd.timestamp);
					test.setLength(timestamp - cmd.getTimestamp());
					test.setReleased(true);
					track.addCmd(test);
					System.out.println("*@"+timestamp+" Note OFF, "+NOTE_NAMES[test.getNote()]+test.getOctave()+" velocity:"+test.getVelocity()+"(MML "+Math.round(test.getVelocity()/ 8.47)+") duration: "+test.getLength());
				}*/
			}
			else{System.out.println("Last note was not "+key+", it was "+savedKey+", ignoring....");}
		}
		else{System.out.println("there was no last note?");}

	}

	private static void createNoteOn(ShortMessage sm, long timestamp, MTrack track) {
		int key = sm.getData1();
		int octave = (key / 12)-1;
		int note = key % 12;
		//String noteName = NOTE_NAMES[note];
		int velocity = sm.getData2();
		long mmlVelocity = Math.round(velocity / 8.47);
		Note cmd = track.getLastNote();
		if(cmd != null){
			if(!cmd.getReleased()){//if note not Note Offed yet..do it now
				cmd.setLength(timestamp - cmd.getTimestamp());
				cmd.setReleased(true);
			}
			else{//if it was released..lets set the delay...if there is one
				cmd.setDelay(timestamp - (cmd.getTimestamp()+cmd.getLength()));
			}
		}

		track.addCmd(new Note(sm.getChannel(), key, note, octave, velocity, timestamp));

		System.out.println("@"+timestamp+" Note On, "+NOTE_NAMES[note]+octave+" key="+key+" velocity: "+velocity+"(MML "+mmlVelocity+")");
	}

	public static class Playlist{
		//int numTracks = 0;
		ArrayList<MTrack> tracks = new ArrayList<MTrack>();

		public void addTrack(MTrack track){
			this.tracks.add(track);
			//this.numTracks = getSize();
		}
		public int getSize(){
			//return tracks.size();
			return tracks.size();
		}
		public MTrack getTrack(int index){
			try{
				return tracks.get(index);
			}
			catch(IndexOutOfBoundsException e){
				return null;
			}

		}

	}

	public static class MTrack{
		int channel;
		long currentTempo;
		long currentLength = -1;
		long currentVelocity = -1;
		int currentOctave = -1;
		//long currentTempo = 120;?
		ArrayList<Object> script = new ArrayList<Object>();


		public MTrack(int trackNum){
			if(trackNum>15){trackNum=15;}
			this.channel = trackNum;
		}
		public void addCmd(Object cmd){
			this.script.add(cmd);

		}
		public Object getCmd(int index){
			try{
				//Note note = (Note) script.get(index);
				return script.get(index);
			}
			catch(IndexOutOfBoundsException e){
				return null;
			}

		}
		public int getSize(){
			return script.size();
		}
		public Object getLastCmd(){
			Object lastCmd = this.getCmd(this.getSize()-1);
			return lastCmd;
		}
		public Note getLastNote(){
			Note note = null;
			for(int i = (script.size()-1);i >= 0;i--){
				if(getCmd(i) instanceof Note){
					note = (Note)getCmd(i);
					break;
				}
			}
			return note;
		}
		public long getCurrentTempo(){
			return currentTempo;
		}
		public long getCurrentLength(){
			return currentLength;
		}
		public long getCurrentVelocity(){
			return currentVelocity;
		}
		public int getCurrentOctave(){
			return currentOctave;
		}
		public void setCurrentTempo(long tempo){
			currentTempo = tempo;
		}
		public void setCurrentLength(long length){
			currentLength = length;
		}
		public void setCurrentVelocity(long velocity){
			currentVelocity = velocity;
		}
		public void setCurrentOctave(int octave){
			currentOctave = octave;
		}
		public String compile(){
			//will read 2 notes at a time to 'pre' read
			Note lastNote = null;
			Note comingNote = null;
			Note nextNote = null;

			String compiledScript = "";
			for(int i=0; i<getSize(); i++){
				if(getCmd(i+1) instanceof Note){nextNote = (Note) getCmd(i+1);}
				else{nextNote = null;}
				boolean lengthNeeded = false;
				switch(getCmd(i).getClass().getSimpleName()){//if(getCmd(i).getClass().getSimpleName().equals("Note")){
				case "Note":
					comingNote = (Note) getCmd(i);
					if(comingNote.getLength() != 0){//do as long as note will play
						//Length checks
						if(comingNote.getLength() != getCurrentLength()){//If track Length isnt same as comming note's
							if(nextNote != null){//if there is a next note
								if(comingNote.getLength() == nextNote.getLength()){//if the coming note's Length is the same as the next's
									if(isDivisible(384, comingNote.getLength())){
										compiledScript += "l"+(384/comingNote.getLength());
									}
									else{
										compiledScript += "L"+comingNote.getLength();
									}
									setCurrentLength(comingNote.getLength());
								}
								else{lengthNeeded = true;}
							}
							else{lengthNeeded = true;}
						}
						//Rest checks
						if(lastNote != null){
							if(lastNote.getDelay() > 0){
								compiledScript += "R";
								if(lastNote.getDelay() != getCurrentLength()){
									compiledScript += lastNote.getDelay();
								}
							}
						}
						else{
							if(comingNote.getTimestamp() > 0){compiledScript += "R"+comingNote.getTimestamp();}
						}
						//Octave checks
						if(comingNote.getOctave() != getCurrentOctave()){//If track Octave isnt same as comming note's
							if(comingNote.getOctave()+1 == getCurrentOctave()){
								compiledScript += ">";setCurrentOctave(comingNote.getOctave());
							}
							else if(comingNote.getOctave()-1 == getCurrentOctave()){
								compiledScript += "<";setCurrentOctave(comingNote.getOctave());
							}
							else{
								compiledScript += "O"+comingNote.getOctave();setCurrentOctave(comingNote.getOctave());
							}
						}
						//Velocity Checks
						if(comingNote.getVelocity() != getCurrentVelocity()){//If track Velocity isnt same as comming note's
							if(isDivisible(384, comingNote.getVelocity())){
								compiledScript += "v"+(384/comingNote.getVelocity());setCurrentVelocity(comingNote.getVelocity());
							}
							else{
								compiledScript += "V"+comingNote.getVelocity();setCurrentVelocity(comingNote.getVelocity());
							}
						}

						//put the note...
						if(isDivisible(384, comingNote.getLength())){
							compiledScript += NOTE_NAMES[comingNote.getKey() % 12];
						}
						else{
							compiledScript += NOTE_NAMES_UPPER[comingNote.getKey() % 12];
						}
						if(lengthNeeded){
							if(comingNote.getLength() == (getCurrentLength()*1.5)){
								compiledScript += ".";
							}
							else{
								if(isDivisible(384, comingNote.getLength())){
									compiledScript += (384/comingNote.getLength());
								}
								else{
									compiledScript += comingNote.getLength();
								}
							}
						}

						lastNote = comingNote;
						nextNote = null;comingNote = null;
					}
					break;
				case "Tempo":
					compiledScript += ((Tempo) getCmd(i)).compile();
					break;
				case "ControllerChange":
					compiledScript += "Y"+((ControllerChange) getCmd(i)).getController()+"-"+((ControllerChange) getCmd(i)).getValue();
					break;
				case "PitchWheel":
					compiledScript += "W"+((PitchWheel) getCmd(i)).getNum()+"-"+((PitchWheel) getCmd(i)).getValue();
					break;
				case "PolyphonicKey":
					compiledScript += "U"+((PolyphonicKey) getCmd(i)).getKey()+"-"+((PolyphonicKey) getCmd(i)).getValue();
					break;
				case "ChannelPressure":
					compiledScript += "S"+((ChannelPressure) getCmd(i)).getValue();
				break;
				}
			}
			return compiledScript;
		}
	}

	public static class Note{
		int channel = 0;
		int key;
		int note;//cdefgab in number value
		int octave;
		int velocity;
		long timestamp;
		long length = 0;//need to get later
		long delay = 0;//need to get later
		boolean released = false;//if was Note Offed

		public Note(int channel,int key,int note,int octave, int velocity, long timestamp){
			this.channel = channel;
			this.key = key;
			this.note = note;
			this.octave = octave;
			this.velocity = velocity;
			this.timestamp = timestamp;
		}
		public long getLength(){
			return length;
		}
		public long getDelay(){
			return delay;
		}
		public int getKey(){
			return key;
		}
		public int getNote(){
			return note;
		}
		public int getOctave(){
			return octave;
		}
		public int getVelocity(){
			return velocity;
		}
		public long getTimestamp(){
			return timestamp;
		}
		public boolean getReleased(){
			return released;
		}
		public void setLength(long length){
			this.length = length;
		}
		public void setDelay(long delay){
			this.delay = delay;
		}
		public void setReleased(boolean release){
			released = release;
		}
	}
	public static class Tempo{
		long value;
		public Tempo(long value){
			this.value = value;
		}
		public long getValue(){
			return this.value;
		}
		public void setValue(int value){
			this.value = value;
		}
		public long getMidiValue(){
			return Math.round(Math.pow(((float)this.value/60000000),-1));
		}
		public String compile(){
			return "T"+getMidiValue();
		}
	}
	public static class ControllerChange{//B
		int controllerNum;
		int value;
		public ControllerChange(int num,int value){
			this.controllerNum = num;
			this.value = value;
		}
		public int getValue(){
			return this.value;
		}
		public void setValue(int value){
			this.value = value;
		}
		public int getController(){
			return this.controllerNum;
		}
		public void setController(int num){
			this.controllerNum = num;
		}
		public String compile(){
			return "Y"+getController()+"-"+getValue();
		}
	}
	public static class PitchWheel{//E
		int num;
		int value;
		public PitchWheel(int num,int value){
			this.num = num;
			this.value = value;
		}
		public int getValue(){
			return this.value;
		}
		public void setValue(int value){
			this.value = value;
		}
		public int getNum(){
			return this.num;
		}
		public void setNum(int num){
			this.num = num;
		}
		public String compile(){
			return "W"+getNum()+"-"+getValue();
		}
	}
	public static class PolyphonicKey{//
		int key;
		int value;
		public PolyphonicKey(int key,int value){
			this.key = key;
			this.value = value;
		}
		public int getValue(){
			return this.value;
		}
		public void setValue(int value){
			this.value = value;
		}
		public int getKey(){
			return this.key;
		}
		public void setKey(int key){
			this.key = key;
		}
		public String compile(){
			return "U"+getKey()+"-"+getValue();
		}
	}
	public static class ChannelPressure{//
		int channel = 0;
		int value;
		public ChannelPressure(int channel,int value){
			this.channel = channel;
			this.value = value;
		}
		public int getValue(){
			return this.value;
		}
		public void setValue(int value){
			this.value = value;
		}
		public String compile(){
			return "S"+getValue();
		}
	}

	public static boolean isDivisible(long number, long byNumber){
		if(byNumber == 0){return false;}
		return (number % byNumber == 0);
	}
}
