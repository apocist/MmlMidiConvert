package com.inverseinnovations.mmlMidiConvert;
//XXX should i +1 all Lengths?
//add ability for instrument change
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
/*
 * , = Break of tracks
 * < = Octave Down
 * > = Octave Up
 * l = Default note length(3ML mml)
 * L = Default note length(direct midi style)
 * v = defualt velocity (3ML mml)
 * V = default velocity (direct midi)
 * tT = tempo
 * oO = defualt Octave
 * N = custom note
 * r = Rest
 * @ = instrument 1 value
 * S = Channel Pressure 1 value
 * Y = Control Change 2 value
 * U = Pnotonic Key 2 value
 * W = Pitch Wheel = 2 value
 */
//import javax.swing.JTextArea;

public class MmlMidiConvert{
	//JTextArea inputField;
	Reader reader;
	Playlist playlist;
	Track track;//should get rid of

	public InputStream parseMML(String input){
		ArrayList<String> inputSplit = new ArrayList<String>(Arrays.asList(input.split(",")));
		playlist = new Playlist();
		int data;

		for(int l = 0;l<inputSplit.size();l++){//process the notes first
			track = new Track(playlist.getSize());
			char[] chars = inputSplit.get(l).toCharArray();
			reader = new CharArrayReader(chars);
			try {
				data = reader.read();
				while(data != -1) {
					startParse(data);
					data = reader.read();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			chars = null;
			playlist.addTrack(track);
		}

		//lets compile it
		ByteArrayList bytesFin = playlist.compile();
		return new ByteArrayInputStream(bytesFin.toByteArray());

	}

	public void startParse(int inital){
		boolean hit = false;//if a note
		boolean hitPureMidi = false;
		int tempVar;
		int tempVar2;
		int nextChar;
		boolean noteHold = false;
		Note note = null;

		//outputField.setText(outputField.getText()+initalChar);
		try {
			switch(inital){
				/*cC*/	case 67:hitPureMidi=true;case 99: note = new Note(track.getChannel(),0,track.getOctave(),track.getVelocity(),track.getLength());hit = true;break;//outputField.setText(outputField.getText()+initalChar);break;
				/*dD*/	case 68:hitPureMidi=true;case 100: note = new Note(track.getChannel(),2,track.getOctave(),track.getVelocity(),track.getLength());hit = true;break;
				/*eE*/	case 69:hitPureMidi=true;case 101: note = new Note(track.getChannel(),4,track.getOctave(),track.getVelocity(),track.getLength());hit = true;break;
				/*fF*/	case 70:hitPureMidi=true;case 102: note = new Note(track.getChannel(),5,track.getOctave(),track.getVelocity(),track.getLength());hit = true;break;
				/*gG*/	case 71:hitPureMidi=true;case 103: note = new Note(track.getChannel(),7,track.getOctave(),track.getVelocity(),track.getLength());hit = true;break;//< 60 / > 62
				/*aA*/	case 65:hitPureMidi=true;case 97: note = new Note(track.getChannel(),9,track.getOctave(),track.getVelocity(),track.getLength());hit = true;break;//012345 starts with 48 being 0/ 57 being 9
				/*bB*/	case 66:hitPureMidi=true;case 98: note = new Note(track.getChannel(),11,track.getOctave(),track.getVelocity(),track.getLength());hit = true;break;//+ = 43  - = 45
				/*<*/	case 60:track.setOctave(track.getOctave()-1);break;
				/*>*/	case 62:track.setOctave(track.getOctave()+1);break;
				/*lL*/	case 76:hitPureMidi=true;case 108://Length of Notes(convert from midi, required)
					reader.mark(3);
					nextChar = reader.read();//mark 1
					if(nextChar >= 49 && nextChar <= 57){//if number between 1-9(cant start with 0..duh)
						tempVar = nextChar-48;
						nextChar = reader.read();//mark 2
						if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
							tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
							nextChar = reader.read();//mark 3
							if(nextChar == 46){//if there is period
								noteHold = true;
							}
						}
						else if(nextChar == 46){
							noteHold = true;
						}
						if(!hitPureMidi){
							if(tempVar>64)tempVar = 64;
							if(tempVar<1)tempVar = 1;
							tempVar = 384/tempVar;
						}
						if(noteHold){tempVar = (int) (tempVar * 1.5);}
						track.setLength(tempVar);
					}
					reader.reset();
					break;
				/*vV*/	case 86:hitPureMidi=true;case 118://Velocity
							reader.mark(3);
							nextChar = reader.read();//mark 1
							if(nextChar >= 49 && nextChar <= 57){//if number between 1-9(cant start with 0..duh)
								tempVar = nextChar-48;
								nextChar = reader.read();//mark 2
								if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
									tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
									nextChar = reader.read();//mark 3
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
									}
								}
								if(!hitPureMidi){
									if(tempVar>64)tempVar = 64;
									if(tempVar<0)tempVar = 0;
									tempVar = (int) Math.floor(tempVar*8.47);
								}
								track.setVelocity(tempVar);
							}
							reader.reset();
							break;
				/*tT*/	case 116:case 84:
							reader.mark(3);
							nextChar = reader.read();//mark 1
							if(nextChar >= 49 && nextChar <= 57){//if number between 1-9(cant start with 0..duh)
								tempVar = nextChar-48;
								nextChar = reader.read();//mark 2
								if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
									tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
									nextChar = reader.read();//mark 3
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
									}
								}
								if(tempVar>255)tempVar = 255;
								if(tempVar<32)tempVar = 32;
								System.out.println("mml temp is "+tempVar);
								track.addCmd(new Tempo(tempVar));
							}
							reader.reset();
							break;
				/*nN*/	case 110:case 78:
							reader.mark(2);
							nextChar = reader.read();//mark 1
							if(nextChar >= 49 && nextChar <= 57){//if number between 1-9(cant start with 0..duh)
								tempVar = nextChar-48;
								nextChar = reader.read();//mark 2
								if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
									tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));

								}
								if(tempVar>96)tempVar = 96;
								if(tempVar<0)tempVar = 0;
								tempVar=tempVar+12;
								track.addCmd(new Note(track.getChannel(),tempVar,0,track.getVelocity(),track.getLength()));
							}
							reader.reset();
							break;
				/*oO*/	case 111:case 79:
							reader.mark(1);
							nextChar = reader.read();//mark 1
							if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
								tempVar = nextChar-48;
								if(tempVar>8)tempVar = 8;
								if(tempVar<0)tempVar = 0;
								track.setOctave(tempVar+1);
							}
							reader.reset();
							break;
				/*rR*/	case 82:hitPureMidi=true;case 114://Rest
							reader.mark(4);
							tempVar = track.getLength();
							nextChar = reader.read();//mark 1
							//tempVar;
							if(nextChar >= 49 && nextChar <= 57){//if number between 1-9(cant start with 0..duh)
								tempVar = nextChar-48;
								nextChar = reader.read();//mark 2
								if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
									tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
									nextChar = reader.read();//mark 3
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
										nextChar = reader.read();//mark 4
									}
								}
								if(!hitPureMidi){
									if(tempVar>64)tempVar = 64;
									if(tempVar<1)tempVar = 1;
									tempVar = 384/tempVar;
								}
							}
							if(nextChar == 46){//if there is period
								noteHold = true;
							}
							if(noteHold){tempVar = (int) (tempVar * 1.5);}
							if(track.getSize()>0){
								Object cmd = track.getLastCmd();
								if(cmd.getClass().getSimpleName().equals("Note")){((Note) cmd).addDelay(tempVar);}
								if(cmd.getClass().getSimpleName().equals("Tempo")){((Tempo) cmd).addDelay(tempVar);}
								if(cmd.getClass().getSimpleName().equals("ControllerChange")){((ControllerChange) cmd).addDelay(tempVar);}
								if(cmd.getClass().getSimpleName().equals("PitchWheel")){((PitchWheel) cmd).addDelay(tempVar);}
								if(cmd.getClass().getSimpleName().equals("PolyphonicKey")){((PolyphonicKey) cmd).addDelay(tempVar);}
								if(cmd.getClass().getSimpleName().equals("ChannelPressure")){((ChannelPressure) cmd).addDelay(tempVar);}
							}
							else{track.addIntialDelay(tempVar);}
							reader.reset();
							break;
				/*@*/	case 64://Instrument
					reader.mark(3);
					nextChar = reader.read();//mark 1
					if(nextChar >= 49 && nextChar <= 57){//if number between 1-9(cant start with 0..duh)
						tempVar = nextChar-48;
						nextChar = reader.read();//mark 2
						if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
							tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
							nextChar = reader.read();//mark 3
							if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
								tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
							}
						}
						if(tempVar>127)tempVar = 127;
						if(tempVar<0)tempVar = 0;
						track.setInstr(tempVar);
					}
					reader.reset();
					break;
				/*sS*/	case 115:case 83://Channel Pressure
					reader.mark(3);
					nextChar = reader.read();//mark 1
					if(nextChar >= 49 && nextChar <= 57){//if number between 1-9(cant start with 0..duh)
						tempVar = nextChar-48;
						nextChar = reader.read();//mark 2
						if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
							tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
							nextChar = reader.read();//mark 3
							if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
								tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
							}
						}
						if(tempVar>127)tempVar = 127;
						if(tempVar<0)tempVar = 0;
						track.setInstr(tempVar);
					}
					reader.reset();
					break;
				/*wW*/	case 119:case 87://Pitch Wheel
					reader.mark(3);
					tempVar2 = 0;
					nextChar = reader.read();//mark 1
					if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
						tempVar = nextChar-48;
						nextChar = reader.read();//mark 2
						if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
							tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
							nextChar = reader.read();//mark 3
							if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
								tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
								nextChar = reader.read();//mark 4
								if(nextChar == 45){//if item is -
									nextChar = reader.read();//mark 5
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										hit = true;
										tempVar2 = nextChar-48;
										nextChar = reader.read();//mark 6
										if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
											tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
											nextChar = reader.read();//mark 7
											if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
												tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
											}
										}
									}
								}
							}
							else if(nextChar == 45){//if item is -
								nextChar = reader.read();//mark 5
								if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
									hit =true;
									tempVar2 = nextChar-48;
									nextChar = reader.read();//mark 6
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
										nextChar = reader.read();//mark 7
										if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
											tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
										}
									}
								}
							}
						}
						else if(nextChar == 45){//if item is -
							nextChar = reader.read();//mark 5
							if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
								hit=true;
								tempVar2 = nextChar-48;
								nextChar = reader.read();//mark 6
								if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
									tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
									nextChar = reader.read();//mark 7
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
									}
								}
							}
						}
						if(hit){
							if(tempVar>127)tempVar = 127;
							if(tempVar<0)tempVar = 0;
							if(tempVar2>127)tempVar2 = 127;
							if(tempVar2<0)tempVar2 = 0;
							track.addCmd(new PitchWheel(track.getChannel(),tempVar, tempVar2));
						}
					}
					hit = false;
					reader.reset();
					break;
				/*uU*/	case 117:case 85://Polyphonic Key
					reader.mark(3);
					tempVar2 = 0;
					nextChar = reader.read();//mark 1
					if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
						tempVar = nextChar-48;
						nextChar = reader.read();//mark 2
						if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
							tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
							nextChar = reader.read();//mark 3
							if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
								tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
								nextChar = reader.read();//mark 4
								if(nextChar == 45){//if item is -
									nextChar = reader.read();//mark 5
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										hit = true;
										tempVar2 = nextChar-48;
										nextChar = reader.read();//mark 6
										if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
											tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
											nextChar = reader.read();//mark 7
											if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
												tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
											}
										}
									}
								}
							}
							else if(nextChar == 45){//if item is -
								nextChar = reader.read();//mark 5
								if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
									hit =true;
									tempVar2 = nextChar-48;
									nextChar = reader.read();//mark 6
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
										nextChar = reader.read();//mark 7
										if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
											tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
										}
									}
								}
							}
						}
						else if(nextChar == 45){//if item is -
							nextChar = reader.read();//mark 5
							if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
								hit=true;
								tempVar2 = nextChar-48;
								nextChar = reader.read();//mark 6
								if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
									tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
									nextChar = reader.read();//mark 7
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
									}
								}
							}
						}
						if(hit){
							if(tempVar>127)tempVar = 127;
							if(tempVar<0)tempVar = 0;
							if(tempVar2>127)tempVar2 = 127;
							if(tempVar2<0)tempVar2 = 0;
							track.addCmd(new PolyphonicKey(track.getChannel(),tempVar, tempVar2));
						}
					}
					hit = false;
					reader.reset();
					break;
				/*yY*/	case 121:case 89://Control Change
					reader.mark(3);
					tempVar2 = 0;
					nextChar = reader.read();//mark 1
					if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
						tempVar = nextChar-48;
						nextChar = reader.read();//mark 2
						if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
							tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
							nextChar = reader.read();//mark 3
							if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
								tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
								nextChar = reader.read();//mark 4
								if(nextChar == 45){//if item is -
									nextChar = reader.read();//mark 5
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										hit = true;
										tempVar2 = nextChar-48;
										nextChar = reader.read();//mark 6
										if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
											tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
											nextChar = reader.read();//mark 7
											if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
												tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
											}
										}
									}
								}
							}
							else if(nextChar == 45){//if item is -
								nextChar = reader.read();//mark 5
								if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
									hit =true;
									tempVar2 = nextChar-48;
									nextChar = reader.read();//mark 6
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
										nextChar = reader.read();//mark 7
										if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
											tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
										}
									}
								}
							}
						}
						else if(nextChar == 45){//if item is -
							nextChar = reader.read();//mark 5
							if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
								hit=true;
								tempVar2 = nextChar-48;
								nextChar = reader.read();//mark 6
								if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
									tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
									nextChar = reader.read();//mark 7
									if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
										tempVar2 = Integer.valueOf(tempVar2+""+Integer.toString(nextChar-48));
									}
								}
							}
						}
						if(hit){
							if(tempVar>127)tempVar = 127;
							if(tempVar<0)tempVar = 0;
							if(tempVar2>127)tempVar2 = 127;
							if(tempVar2<0)tempVar2 = 0;
							track.addCmd(new ControllerChange(track.getChannel(),tempVar, tempVar2));
						}
					}
					hit = false;
					reader.reset();
					break;
				default: break;
			}
			if(hit==true){//if NOTE
				reader.mark(6);
				nextChar = reader.read();//mark 1
				if(nextChar == 43){//+
					note.setNote(note.getNote()+1);//increase by 1
					nextChar = reader.read();//mark 2
				}
				else if(nextChar == 45){//-

					note.setNote(note.getNote()-1);//decrease by 1
					nextChar = reader.read();//mark 2
				}
				if(nextChar >= 49 && nextChar <= 57){//if number between 1-9(cant start with 0..duh)
					tempVar = nextChar-48;
					nextChar = reader.read();//mark 3
					if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
						tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
						nextChar = reader.read();//mark 4
						if(nextChar >= 48 && nextChar <= 57){//if number between 0-9
							tempVar = Integer.valueOf(tempVar+""+Integer.toString(nextChar-48));
							nextChar = reader.read();//mark 5
						}
					}
					if(!hitPureMidi){
						if(tempVar>64)tempVar = 64;
						if(tempVar<1)tempVar = 1;
						tempVar = 384/tempVar;
					}
					note.setLength(tempVar);
				}
				if(nextChar == 46){//if there is period
					note.setLength((int)(note.getLength()*1.5));
					nextChar = reader.read();//mark 6
				}
				if(track.ampersand != null){//if & is set
					if(track.getLastCmd().getClass().getSimpleName().equals("Note")){
						if(((Note) track.getLastCmd()).getOctave()==note.getOctave()&&((Note) track.getLastCmd()).getNote()==note.getNote()){
							((Note) track.getLastCmd()).addLength(note.getLength());
							track.ampersand = null;
						}
						else{
							track.addCmd(note);
						}
					}
					else{
						track.addCmd(note);
					}
				}
				else{
					track.addCmd(note);
				}
				if(nextChar == 38){//if there is &
					track.ampersand = note;
				}
				reader.reset();

			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}


	public ByteArrayList convertToHex(int number){
		ByteArrayList state = new ByteArrayList();
		state.append(number);
		return state;
	}

	public ByteArrayList convertTo128Hex(int number){//FIXME only goes to 2 bytes...need to extend later or run into issues
		ByteArrayList state = new ByteArrayList();
		byte[] bytes;// = new byte[]{0x00,0x00};
		if(number>127){
			int upper=0;
			int middle=0;
			int lower = number;

			if(lower>16383){
				upper = 129;
				lower = lower - 16384;
			}
			if(lower>127){
				if(upper==0){
					middle=129;
				}
				else{
					middle=128;
				}
				lower=lower-128;
			}
			while(lower>127){
				lower=lower-128;
				middle++;
				if(middle>255){
					upper++;
					middle=128;
				}
			}
			if(upper>0){
				bytes = new byte[]{(byte)upper,(byte)middle,(byte)lower};
			}
			else{
				bytes = new byte[]{(byte)middle,(byte)lower};
			}
			state.append(bytes);
			//do this..convert
			/*number = number-128;
			bytes[0]=(byte) 129;
			while(number>127){
				number = number-128;
				bytes[0]++;
			}
			//now number is below 128
			bytes[1] = (byte) number;
			//checks?
			state.append(bytes[0]);
			state.append(bytes[1]);*/
		}
		else{
			state.append(number);
		}
		return state;
	}

	public static InputStream main(String args) {
		//@SuppressWarnings("unused")
		MmlMidiConvert convert = new MmlMidiConvert();
		return convert.parseMML(args);
	}

	public class Playlist{
		ByteArrayList header = new ByteArrayList(new byte[]{0x4d,0x54,0x68,0x64,0x00,0x00,0x00,0x06,0x00,0x01});
		int numTracks = 0;
		ByteArrayList deltaTime = new ByteArrayList(new byte[]{0x00,0x60});
		ArrayList<Track> tracks = new ArrayList<Track>();

		public void addTrack(Track track){
			this.tracks.add(track);
			this.numTracks = getSize();
		}
		public Track getTrack(int index){
			try{
				//Note note = (Note) script.get(index);
				return tracks.get(index);
			}
			catch(IndexOutOfBoundsException e){
				return null;
			}

		}
		public int getSize(){
			return tracks.size();
		}
		public ByteArrayList compile(){
			ByteArrayList allPlaylist = new ByteArrayList();
			ByteArrayList numTrack = new ByteArrayList();
			ByteArrayList numTrackTemp = new ByteArrayList();
			numTrack.append(convertToHex(numTracks));

			allPlaylist.append(header);
			for(int size = numTrack.size();size<2;size = numTrack.size()){
				numTrackTemp = numTrack;
				numTrack = new ByteArrayList(new byte[]{0x00});
				numTrack.append(numTrackTemp);
			}
			allPlaylist.append(numTrack);
			allPlaylist.append(deltaTime);
			for(int l = 0;l<getSize();l++){
				allPlaylist.append(getTrack(l).compile());
			}
			return allPlaylist;
		}
	}

	public class Track{
		int channel;
		ByteArrayList header = new ByteArrayList(new byte[]{0x4d,0x54,0x72,0x6b});
		ByteArrayList trackSize = new ByteArrayList(new byte[]{0x00,0x00,0x00,(byte) 0x8d});//need a 00 after
		ByteArrayList timeSig = new ByteArrayList(new byte[]{(byte) 0xff,0x58,0x04,0x04,0x02,0x18,0x08,0x00});
		//ByteArrayList trackName1 = new ByteArrayList(new byte[]{(byte)0xff,0x03,0x08,0x55,0x6e,0x74,0x69,0x74,0x6c,0x65,0x64,0x00});//00 was added after
		ByteArrayList status = new ByteArrayList(new byte[]{(byte) 0xf0,0x05,0x7e,0x7f,0x09,0x01,(byte) 0xf7,0x00});//00 was added after
		//ByteArrayList trackName2 = new ByteArrayList(new byte[]{(byte) 0xff,0x03,0x05,0x54,0x72,0x61,0x63,0x6b,0x00});//00 was added after
		int initialDelay = 0;
		ArrayList<Object> script = new ArrayList<Object>();
		ByteArrayList trackEnd = new ByteArrayList(new byte[]{(byte) 0xff,0x2f,0x00});

		int instrument = 25;//mandolin default
		int currentOctave = 5;
		int currentVelocity = 67;
		int currentLength = 96;
		//int tempo = 64;//unknown what default is so far
		Note ampersand = null;

		public Track(int trackNum){
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
		public int getChannel(){
			return channel;
		}
		public int getInstr(){
			return instrument;
		}
		public void setInstr(int instr){
			this.instrument = instr;
		}
		public int getOctave(){
			return currentOctave;
		}
		public void setOctave(int octave){
			this.currentOctave = octave;
		}
		public void setVelocity(int v){
			this.currentVelocity = v;
		}
		public int getVelocity(){
			return currentVelocity;
		}
		public int getLength(){
			return currentLength;
		}
		public void setLength(int length){
			this.currentLength = length;
		}
		public void addIntialDelay(int delay){
			this.initialDelay = this.initialDelay + delay;
		}
		public int getMidiDelay(){
			return this.initialDelay;
		}
		public int getSize(){
			return script.size();
		}
		public Object getLastCmd(){
			Object lastCmd = this.getCmd(this.getSize()-1);
			return lastCmd;
		}
		public ByteArrayList compile(){
			//initialDelay was not added?
			ByteArrayList allTrack = new ByteArrayList();
			ByteArrayList trackSize = new ByteArrayList();
			ByteArrayList trackSizeTemp = new ByteArrayList();
			ByteArrayList compiledScript = new ByteArrayList();
			compiledScript.append(new byte[]{0x00});
			if(channel==0){
				compiledScript.append(timeSig);
				compiledScript.append(status);
			}
			//compiledScript.append(unknownProg);
			//compiledScript.append(new byte[]{(byte) (176+getChannel()),0x00,0x00,0x00,(byte) (176+getChannel()),0x20,0x00,0x00,(byte) (192+getChannel()),(byte) getInstr(),0x00,(byte) (176+getChannel()),0x07,0x64,0x00,(byte) (176+getChannel()),0x0a,0x40});
			//compiledScript.append(new byte[]{(byte) (192+getChannel()),(byte) getInstr(),0x00});
			compiledScript.append(new byte[]{(byte) (192+getChannel()),(byte) getInstr()});
			compiledScript.append(convertTo128Hex(initialDelay));
			for(int i=0; i<getSize(); i++){
				if(getCmd(i).getClass().getSimpleName().equals("Note")){
					compiledScript.append(((Note) getCmd(i)).compile());
				}
				else if(getCmd(i).getClass().getSimpleName().equals("Tempo")){
					compiledScript.append(((Tempo) getCmd(i)).compile());
				}
				else if(getCmd(i).getClass().getSimpleName().equals("ControllerChange")){
					compiledScript.append(((ControllerChange) getCmd(i)).compile());
				}
				else if(getCmd(i).getClass().getSimpleName().equals("PitchWheel")){
					compiledScript.append(((PitchWheel) getCmd(i)).compile());
				}
				else if(getCmd(i).getClass().getSimpleName().equals("PolyphonicKey")){
					compiledScript.append(((PolyphonicKey) getCmd(i)).compile());
				}
				else if(getCmd(i).getClass().getSimpleName().equals("ChannelPressure")){
					compiledScript.append(((ChannelPressure) getCmd(i)).compile());
				}
			}
			compiledScript.append(trackEnd);

			trackSize.append(convertToHex(compiledScript.size()));
			for(int size = trackSize.size();size<4;size = trackSize.size()){
				trackSizeTemp = trackSize;
				trackSize = new ByteArrayList(new byte[]{0x00});
				trackSize.append(trackSizeTemp);
			}
			allTrack.append(header);
			allTrack.append(trackSize);

			allTrack.append(compiledScript);

			return allTrack;
		}
	}

	public class Note{
		int channel = 0;
		int note;//cdefgab in number value
		int octave;
		int velocity;
		int length;
		int delay = 0;

		public Note(int channel,int note,int octave, int velocity, int length){
			this.channel = channel;
			this.note = note;
			this.octave = octave;
			this.velocity = velocity;
			this.length = length;
		}
		public int getNote(){
			return note;
		}
		public void setNote(int note){
			this.note = note;
		}
		public int getOctave(){
			return octave;
		}
		public void setOctave(int octave){
			this.octave = octave;
		}
		public int getVelocity(){
			return this.velocity;
		}
		public int getMidiLength(){
			return (int) (384/this.length);
		}
		public int getLength(){
			return length;
		}
		public void setLength(int length){
			this.length = length;
		}
		public void addLength(int length){
			this.length = this.length + length;
		}
		public void addDelay(int delay){
			this.delay = this.delay + delay;
		}
		public int getMidiDelay(){
			return this.delay;
		}
		public int getMidiNote(){
			int midi = getOctave()*12 + note;
			if(midi>127)midi = 127;
			if(midi<0)midi = 0;
			return midi;
		}
		public ByteArrayList compile(){


			ByteArrayList note = new ByteArrayList(new byte[]{(byte)(144+channel)});
			note.append(getMidiNote());
			note.append(convertToHex(getVelocity()));
			//note.append(getMidiLength());
			note.append(convertTo128Hex(getLength()));
			note.append(new byte[]{(byte)(128+channel)});
			note.append(getMidiNote());
			note.append(new byte[]{0x00});
			note.append(convertTo128Hex(getMidiDelay()));

			return note;
		}
	}
	public class Tempo{
		int value;
		int delay;
		public Tempo(int value){
			this.value = value;
		}
		public int getValue(){
			return this.value;
		}
		public void setValue(int value){
			this.value = value;
		}
		public int getMidiValue(){
			return (int) (60000000*Math.pow(this.value,-1));
		}
		public void addDelay(int delay){
			this.delay = this.delay + delay;
		}
		public int getMidiDelay(){
			return this.delay;
		}
		public ByteArrayList compile(){
			ByteArrayList tempo = new ByteArrayList(new byte[]{(byte) 0xff,0x51});
			tempo.append(convertToHex(convertToHex(getMidiValue()).size()));
			System.out.println("getMidiValue() is "+getMidiValue());
			System.out.println("convertToHex(getMidiValue()) is "+convertToHex(getMidiValue()));
			tempo.append(convertToHex(getMidiValue()));
			tempo.append(convertTo128Hex(delay));
			return tempo;
		}
	}
	public class ControllerChange{//B
		int channel = 0;
		int controllerNum;
		int value;
		int delay;
		public ControllerChange(int channel,int num,int value){
			this.controllerNum = num;
			this.channel = channel;
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
		public void addDelay(int delay){
			this.delay = this.delay + delay;
		}
		public int getMidiDelay(){
			return this.delay;
		}
		public ByteArrayList compile(){
			ByteArrayList note = new ByteArrayList(new byte[]{(byte)(176+channel)});
			note.append(convertToHex(getController()));
			//note.append(getMidiLength());
			note.append(convertTo128Hex(getValue()));
			note.append(convertTo128Hex(getMidiDelay()));
			return note;
		}
	}
	public class PitchWheel{//E
		int channel = 0;
		int num;
		int value;
		int delay;
		public PitchWheel(int channel,int num,int value){
			this.num = num;
			this.channel = channel;
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
		public void addDelay(int delay){
			this.delay = this.delay + delay;
		}
		public int getMidiDelay(){
			return this.delay;
		}
		public ByteArrayList compile(){
			ByteArrayList note = new ByteArrayList(new byte[]{(byte)(176+channel)});
			note.append(convertToHex(getNum()));
			//note.append(getMidiLength());
			note.append(convertTo128Hex(getValue()));
			note.append(convertTo128Hex(getMidiDelay()));
			return note;
		}
	}
	public class PolyphonicKey{//
		int channel = 0;
		int key;
		int value;
		int delay;
		public PolyphonicKey(int channel,int key,int value){
			this.key = key;
			this.channel = channel;
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
		public void addDelay(int delay){
			this.delay = this.delay + delay;
		}
		public int getMidiDelay(){
			return this.delay;
		}
		public ByteArrayList compile(){
			ByteArrayList note = new ByteArrayList(new byte[]{(byte)(176+channel)});
			note.append(convertToHex(getKey()));
			//note.append(getMidiLength());
			note.append(convertTo128Hex(getValue()));
			note.append(convertTo128Hex(getMidiDelay()));
			return note;
		}
	}
	public class ChannelPressure{//
		int channel = 0;
		int value;
		int delay;
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
		public void addDelay(int delay){
			this.delay = this.delay + delay;
		}
		public int getMidiDelay(){
			return this.delay;
		}
		public ByteArrayList compile(){
			ByteArrayList note = new ByteArrayList(new byte[]{(byte)(176+channel)});
			note.append(convertTo128Hex(getValue()));
			note.append(convertTo128Hex(getMidiDelay()));
			return note;
		}
	}

	public class ByteArrayList extends ArrayList<Byte>{
		private static final long serialVersionUID = 1L;

		public ByteArrayList(){
			super();
		}

		public ByteArrayList(byte[] byteArray){
			super();
			for(byte b : byteArray){
				this.add(b);
			}
		}

		public void append(byte[] byteArray){
			for(byte b : byteArray){
				this.add(b);
			}
		}

		public void append(ByteArrayList arrayList){
			for(byte b : arrayList){
				this.add(b);
			}
		}
		public void append(int i){
			append(BigInteger.valueOf(i).toByteArray());
		}
		public void append(String i){
			append(String.valueOf(i).getBytes());
		}

		public byte[] toByteArray(){
			byte[] byteArray = new byte[this.size()];
			for(int i = 0; i<this.size(); i++){
				byteArray[i] = this.get(i);
			}
			return byteArray;
		}
	}
}