f = ReadFile("Config.ini")
  w=Int(ReadLine(f))
  h=Int(ReadLine(f))
  d=Int(ReadLine(f))
  mde=Int(ReadLine(f))
  Global fs=Float(ReadLine(f))
  Global playername$ = ReadLine(f)
  Global snd_enable = Int(ReadLine(f))
CloseFile(f)

Graphics3D w, h, d, mde
SetBuffer BackBuffer()

font = LoadFont("Arial", fs)
SetFont font

;===========TYPES, GLOBALS AND CONSTANTS==============
Const USERT = 1
Const BOTT   = 2
Const SHOTT = 3

Collisions USERT, SHOTT, 2, 1
Collisions BOTT, SHOTT, 2, 1
Collisions SHOTT, SHOTT, 2, 1

Const Max_Shots = 250

Global shot_sprite=LoadSprite("Images/sprite.bmp")
HideEntity shot_sprite

Global pricel = LoadImage("Images/Pricel.bmp")
MaskImage pricel, 0, 0, 0
MidHandle pricel
Global shots
Global booms, boom[1000], boomtex = LoadTexture("Images/boom.bmp"), boomsizes[1000]

Type TShot
  Field entity
  Field dist#
  Field damage#
  Field owner#
End Type

Type TPlayer
  Field Name$
  Field Entity
  ;Position
  Field X#
  Field Y#
  Field Z#
  ;Bent (íàêëîí)
  Field Xa#
  Field Ya#
  Field Za#
  ;Moving speed
  Field Speed#
  ;Maximum speed that player cam move with
  Field MaxSpd#
  ;Parameters
  Field HP#  
  Field MaxItems#
  Field Money#
  Field Armor#
  Field ProtectionR#
  Field Damage#
  Field PickR#
  Field Ammo#
End Type

Type TPlanet
  Field Name$
  Field Entity
  Field Money#
  ;Orbit coords
  Field X#
  Field Y#
  Field Z#  
  ;Planet scale
  Field PScale#
  ;Orbit form
  Field Width#
  Field Height#
  Field Depth#
  ;Angles
  Field Xa#
  Field Ya#
  Field Za#
End Type

Type TItem
  Field ItemType$ ;Armor, Ammo, Weapon, Scaner, Battery, etc.
  Field Entity
  ;Float-valued parameters
  Field FValue0#
  Field FValue1#
  Field FValue2#
  Field FValue3#
  Field FValue4#
  Field FValue5#
  ;String-valued parameters
  Field SValue0$
  Field SValue1$
  Field SValue2$
  Field SValue3$
  Field SValue4$
  Field SValue5$
  ;Trade params
  Field Price#
End Type

Type TBot
  Field Name$
  Field Entity
  ;Position
  Field X#
  Field Y#
  Field Z#
  ;Angles
  Field Xa#
  Field Ya#
  Field Za#
  ;Target
  Field Xt#
  Field Yt#
  Field Zt#
  Field Target# ;Entity
  ;Radiuses of View, Attack, Run(Back)
  Field ViewR#
  Field AttackR#
  Field RunR#
  ;Ship data
  Field HP#
  Field Armor#
  Field Weapon$
  Field Ammo#
  Field Damage#
  Field InventoryNum#
  Field ItemsCnt#
  Field Speed#
  Field MaxSpeed#
  Field Accuracy#
  Field Money#
  Field Character# ;0..25 - Enemy, 25..75 - Neutral, 75..100 - Friend
  Field BotType# ;0..25 - Killer, 25..50 - Trader, 50..75 - Fighter, 75..100 - Scinetist
  Field Race# ;0..30 - Human, 30..60 - Alien, 60..100 - Invader
  ;Shooting
  Field mesh#
  Field surf#
  Field brush#
  Field AccuracyNow#
  Field TargetOk#
  Field Dead#
  Field sphere#
End Type

Type TQuest
  Field Condition# ;0 - no condition, 1 - goto (X, Y, Z)+/-R, 2 - bring item(2/3/4 items) here, 3 - bring to him (FValue0# - type, FValue1# - R, SValue2$ - name)
  Field GiverType# ;0 - Bot, 1 - Planet
  Field Giver$
  Field SValue0$ ;Quest text
  Field SValue1$
  Field SValue2$
  Field SValue3$
  Field SValue4$
  Field SValue5$
  Field SValue6$
  Field FValue0#
  Field FValue1#
  Field FValue2#
  Field FValue3#
  Field FValue4#
  Field FValue5#
  Field FValue6#
  Field Done#     ;0 - no, 1 - Done!
  Field Active# ;0 - no, 1 - Active!
End Type

Global user.TPlayer, camera, light, compass, sky, snd1, snd2, snd3, snd4, snd5, m = 1, backgrnd
Global engine_snd#, shoot1_snd#, shoot2_snd#, boom1_snd#, boom2_snd#, boom3_snd#
Global PlanetsCnt, ItemCnt, BotCnt, InventoryItemCnt, QuestCnt, PlanetItemCnt = 5
Global Planet.TPlanet[100], Item.TItem[100], Bot.TBot[100], Inventory.TItem[100], Quest.TQuest[100], PlanetInventory.TItem[50]
Dim BotItems.TItem(1000, 100) ;(Bot, ItemNum)
Dim maina$(4)
Dim opta$(4), optsa$(10), optsag$(10), optsad$(10), optsar$(10), optsgoa$(10)

Function outtext(msg$)
  RenderWorld()

  DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)  
  Color 255, 255, 0  
  Text GraphicsWidth()/2 - (StringWidth(msg$)/2), GraphicsHeight()/2 - (StringHeight(msg$)/2), msg$
  
  Flip()
End Function

Function create_world()
  font = LoadFont("Arial", 16, 1)
  SetFont font
  
  user = New TPlayer
  
  outtext("User object created")
  outtext("Loading user")
 
  ;user\Entity = LoadMesh("Meshes/Alien Saucer.x") : tx# = LoadTexture("Meshes/tx06.dds")
  ;user\Entity = LoadMesh("Meshes/Gray Recon.x") : tx# = LoadTexture("Meshes/fighter2.dds")
  ;user\Entity = LoadMesh("Meshes/Gray Saucer.x") : tx# = LoadTexture("Meshes/tx06a.dds")
  ;Then user\Entity = LoadMesh("Meshes/Gray Stealth.x") : tx# = LoadTexture("Meshes/wraith2.dds")
  ;Then user\Entity = LoadMesh("Meshes/Green Recon.x") : tx# = LoadTexture("Meshes/fighter1.dds")
  ;user\Entity = LoadMesh("Meshes/Red Stealth.x") : tx# = LoadTexture("Meshes/wraith.dds")
  user\Entity = LoadMesh("Meshes/Gray Recon.x")
  tx# = LoadTexture("Meshes/fighter2.bmp")
  EntityTexture user\Entity, tx#
  ScaleEntity user\Entity, 0.1, 0.1, 0.1
  PositionEntity user\Entity, 0, -5, 0

  outtext("User entity loaded")
  
  user\x# = 0
  user\y# = 0
  user\z# = 0
  user\xa# = 0
  user\ya# = 0
  user\za# = 0
  user\MaxSpd# = 25
  user\HP# = 100
  user\Armor# = 100
  user\ProtectionR# = 7.5
  user\Damage# = 10
  user\PickR# = 55
  user\Name$ = playername$

  outtext("User params are set")  
  
  EntityType user\Entity, USERT
  PositionEntity user\Entity, user\x#, user\y#, user\z#
  RotateEntity user\Entity, user\xa#, user\ya#, user\za#
  ;EntityColor user\Entity, 5, 5, 145
  EntityPickMode user\Entity, 1

  outtext("User entity properties are set")
  outtext("Loading camera")

  camera = CreateCamera()
  PositionEntity camera, user\x#, user\y#+5, user\z#-5  
  EntityParent camera, user\Entity
  CameraRange camera, 0.1, 7000

  outtext("Camera created")
  outtext("Loading light")

  light = CreateLight()
  LightColor light, 255, 255, 255
  PositionEntity light, 0, 25, 0

  outtext("Light created")
  outtext("Loading sky")
  
  sky = CreateEntity()
  ScaleEntity sky, 5000, 5000, 5000
  FlipMesh sky
  tex = LoadTexture("Images/backdrop.bmp")
  EntityTexture sky, tex

  outtext("Sky created")
  outtext("Loading planets")

  f = ReadFile("Data/World.map")
    PlanetsCnt = Int(ReadLine(f))
  
  For i=1 To PlanetsCnt
  Planet[i] = New TPlanet
    
  Planet[i]\Name$ = ReadLine(f)    
  Planet[i]\x# = Float(ReadLine(f))
  Planet[i]\y# = Float(ReadLine(f))
  Planet[i]\z# = Float(ReadLine(f))  
  Planet[i]\PScale# = Float(ReadLine(f))
  Planet[i]\Width# = Float(ReadLine(f))
  Planet[i]\Height# = Float(ReadLine(f))
  Planet[i]\Depth# = Float(ReadLine(f))
  Planet[i]\Xa# = 0
  Planet[i]\Ya# = 0
  Planet[i]\Za# = 0
  Planet[i]\Money# = 5000000

  Planet[i]\Entity = CreateEntity()
  tex = LoadTexture("Images/earth.bmp")
  EntityTexture Planet[i]\Entity, tex
  FreeTexture tex
  EntityPickMode Planet[i]\Entity, 2
  ScaleEntity Planet[i]\Entity, Planet[i]\PScale#, Planet[i]\PScale#, Planet[i]\PScale#
  PositionEntity Planet[i]\Entity, Planet[i]\X#, Planet[i]\Y#, Planet[i]\Z#

  outtext("Planet #"+Str(i)+" created")
  Next
  
  CloseFile(f)
  
  outtext("Loading bots")

  f = ReadFile("Data/Bots.map")
    BotCnt = Int(ReadLine(f))

  For i = 1 To BotCnt
    Bot[i] = New TBot    

    Bot[i]\Name$ = ReadLine(f)
    Bot[i]\X# = Float(ReadLine(f))
    Bot[i]\Y# = Float(ReadLine(f))
    Bot[i]\Z# = Float(ReadLine(f))
    
    Bot[i]\Xa# = Float(ReadLine(f))
    Bot[i]\Ya# = Float(ReadLine(f))
    Bot[i]\Za# = Float(ReadLine(f))
    
    Bot[i]\Xt# = Float(ReadLine(f))
    Bot[i]\Yt# = Float(ReadLine(f))
    Bot[i]\Zt# = Float(ReadLine(f))
    
    Bot[i]\ViewR# = Float(ReadLine(f))
    Bot[i]\AttackR# = Float(ReadLine(f))
    Bot[i]\RunR# = Float(ReadLine(f))

    Bot[i]\HP# = Float(ReadLine(f))
      Bot[i]\Armor# = Float(ReadLine(f))
      Bot[i]\Weapon$ = ReadLine(f)
      Bot[i]\Ammo# = Float(ReadLine(f))
      Bot[i]\Damage# = Float(ReadLine(f))
    Bot[i]\Accuracy# = Float(ReadLine(f))
      Bot[i]\InventoryNum# = Float(ReadLine(f))
    Bot[i]\ItemsCnt# = Float(ReadLine(f))    

    For t = 1 To Bot[i]\ItemsCnt#
      BotItems(i, t) = New TItem
    BotItems(i, t)\SValue0$ = ReadLine(f)       
    Next

    Bot[i]\Character# = Float(ReadLine(f))
    Bot[i]\BotType# = Float(ReadLine(f))
    Bot[i]\Race# = Float(ReadLine(f))

    If Bot[i]\Character# < 25
      Bot[i]\Entity = LoadMesh("Meshes/Alien Saucer.x")
      tx# = LoadTexture("Meshes/tx06.bmp")
    ElseIf (Bot[i]\Character# >= 25) And (Bot[i]\Character# <= 75)
      Bot[i]\Entity = LoadMesh("Meshes/Gray recon.x")
      tx# = LoadTexture("Meshes/fighter2.bmp")
    ElseIf Bot[i]\Character# > 75
      Bot[i]\Entity = LoadMesh("Meshes/Red stealth.x")
      tx# = LoadTexture("Meshes/wraith.bmp")
    EndIf
        
    EntityTexture Bot[i]\Entity, tx#
    DebugLog "Bot[" + Str(i) + "]\\Entity=" + Str(Bot[i]\Entity)
    ScaleEntity Bot[i]\Entity, 0.1, 0.1, 0.1

    Bot[i]\TargetOk# = 0
    Bot[i]\Target = 0
    Bot[i]\Dead# = 0
    Bot[i]\Money# = 100000

    Bot[i]\Sphere = CreateEntity()
    DebugLog "Bot[" + Str(i) + "]\\Sphere=" + Str(Bot[i]\Entity)
    ScaleEntity Bot[i]\Sphere, 5, 5, 5
    EntityAlpha Bot[i]\Sphere, 0.075
    EntityPickMode Bot[i]\Sphere, 1

    EntityType Bot[i]\Entity, BOTT
    RotateEntity Bot[i]\Entity, Bot[i]\Xa#, Bot[i]\Ya#, Bot[i]\Za#
    PositionEntity Bot[i]\Entity, Bot[i]\X#, Bot[i]\Y#, Bot[i]\Z#
    EntityPickMode Bot[i]\Entity, 1

    ;If Bot[i]\Character# < 25 Then EntityColor Bot[i]\Entity, 255, 0, 0 Else
    ;If (Bot[i]\Character# >= 25) And (Bot[i]\Character# <= 75) Then EntityColor Bot[i]\Entity, 255,255,0 Else
    ;If Bot[i]\Character# > 75 Then EntityColor Bot[i]\Entity, 0, 255, 0 Else

    outtext("Bot #"+Str(i)+" created")
  Next
  CloseFile(f)

  outtext("Loading items")

  f = ReadFile("Data/Items.map")
    ItemCnt = Int(ReadLine(f))

  For i = 1 To ItemCnt
    Item[i] = New TItem
    
    Item[i]\SValue0$ = ReadLine(f)
    Item[i]\SValue1$ = ReadLine(f)
    Item[i]\FValue0# = Float(ReadLine(f))
    Item[i]\SValue2$ = ReadLine(f)
    Item[i]\FValue1# = Float(ReadLine(f))

    outtext("Item #"+Str(i)+" loaded")
  Next
  CloseFile(f)

  outtext("Creating user inventory")
  
  For i = 1 To 5
    InventoryItemCnt = i
    Inventory[i] = New TItem
    Inventory[i] = Item[Rnd(ItemCnt)+1]
  Next

  outtext("User inventory created")
  outtext("Loading bot items")

  For i = 1 To BotCnt
      For t = 1 To Bot[i]\ItemsCnt#      
        For j = 1 To ItemCnt
      If Item[j]\SValue0$ = BotItems(i, t)\SValue0$
        BotItems(i, t)\SValue0$ = Item[j]\SValue0$
          BotItems(i, t)\SValue1$ = Item[j]\SValue1$
          BotItems(i, t)\FValue0# = Item[j]\FValue0#
          BotItems(i, t)\SValue2$ = Item[j]\SValue2$
          BotItems(i, t)\FValue1# = Item[j]\FValue1#

      If Item[j]\SValue1$ = "Protection" Then Bot[i]\Armor# = Bot[i]\Armor# + Item[j]\FValue0#
      If Item[j]\SValue1$ = "Speed" Then Bot[i]\MaxSpeed# = Bot[i]\MaxSpeed# + Item[j]\FValue0#
      If Item[j]\SValue1$ = "View" Then Bot[i]\ViewR# = Bot[i]\ViewR# + Item[j]\FValue0#
      If Item[j]\SValue1$ = "Damage" Then Bot[i]\Damage# = Bot[i]\Damage# + Item[j]\FValue0#
      If Item[j]\SValue1$ = "Count" Then Bot[i]\Ammo# = Bot[i]\Ammo# + Item[j]\FValue0#
      
        Exit
      EndIf  
    Next  
    Next
  Next

  outtext("Bot items created")
  outtext("Loading quests")

  f = ReadFile("Data/Quest.map")
    QuestCnt = Int(ReadLine(f))

    For i = 1 To QuestCnt
      Quest[i] = New TQuest        
    Quest[i]\GiverType# = Float(ReadLine(f))    
    Quest[i]\Giver$ = ReadLine(f)
    Quest[i]\Condition# = Float(ReadLine(f))

    k# = Float(ReadLine(f))    
    If k# >= 1 Then Quest[i]\SValue0$ = ReadLine(f)
    If k# >= 2 Then Quest[i]\SValue1$ = ReadLine(f)
    If k# >= 3 Then Quest[i]\SValue2$ = ReadLine(f)
    If k# >= 4 Then Quest[i]\SValue3$ = ReadLine(f)
    If k# >= 5 Then Quest[i]\SValue4$ = ReadLine(f)
    If k# = 6 Then Quest[i]\SValue5$ = ReadLine(f)

    k# = Float(ReadLine(f))
    If k# >= 1 Then Quest[i]\FValue0# = Float(ReadLine(f))
    If k# >= 2 Then Quest[i]\FValue1# = Float(ReadLine(f))
    If k# >= 3 Then Quest[i]\FValue2# = Float(ReadLine(f))
    If k# >= 4 Then Quest[i]\FValue3# = Float(ReadLine(f))
    If k# >= 5 Then Quest[i]\FValue4# = Float(ReadLine(f))
    If k# = 6 Then Quest[i]\FValue5# = Float(ReadLine(f))

    Quest[i]\Active# = 0
    Quest[i]\Done# = 0
    Next
  CloseFile(f)

  outtext("Quests loaded")

  If snd_enable = 1
  outtext("Loading sounds...")

  engine_snd# = LoadSound("Sounds/Jet loop.wav")
  outtext("Sound #1 loaded")
  shoot1_snd# = LoadSound("Sounds/Lazer 1.wav")
  outtext("Sound #2 loaded")
  shoot2_snd# = LoadSound("Sounds/Invader 1.wav")
  outtext("Sound #3 loaded")
  boom1_snd# = LoadSound("Sounds/Powerup.wav")
  outtext("Sound #4 loaded")
  boom2_snd# = LoadSound("Sounds/Plane.wav")
  outtext("Sound #5 loaded")
  boom3_snd# = LoadSound("Sounds/Motor.wav")
  outtext("Sound #6 loaded")

  snd1 = LoadSound("Music/music1.mp3")
  outtext("Music #1 loaded")
    snd2 = LoadSound("Music/music2.mp3")
  outtext("Music #2 loaded")
    snd3 = LoadSound("Music/music3.mp3")
  outtext("Music #3 loaded")
    snd4 = LoadSound("Music/music4.mp3")
  outtext("Music #4 loaded")
    snd5 = LoadSound("Music/music5.mp3")
  outtext("Music #5 loaded")

  outtext("All sounds loaded")
  EndIf
  
  outtext("Starting game...")

  update_user_params()
End Function

Function main_menu()  
  maina$(1) = "NEW GAME"
  maina$(2) = "LOAD GAME"
  maina$(3) = "OPTIONS"
  maina$(4) = "EXIT"
  mi# = 1
  backgrnd = LoadImage("Images/bd01.bmp")
  Local font = LoadFont("Arial", 18, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font
  
  While True
    ; SetFont font
    ; RenderWorld()

    ; GetKey()
  
    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)

	For i = 1 To 4
        If i = mi#
            Color 255, 128, 0
        Else
            Color 255, 255, 255
        EndIf
        
        Text GraphicsWidth()/2, (GraphicsHeight()/2)+(i * FontHeight() * 1.25), maina$(i), True, True
    Next

    If KeyHit(208) Then mi# = mi# + 1
	If KeyHit(200) Then mi# = mi# - 1

  If mi# < 1 Then mi# = 1
  If mi# > 4 Then mi# = 4
  
  If KeyHit(28)
    If mi# = 1 Then SetFont font0 : create_world() : Return Else
    If mi# = 2 Then load_game(enter_text_dialog("Enter name of loaded save:", 25)+".ais")
    If mi# = 3 Then options_menu()
    
    If (mi# = 4)
      message("This is way to a real world!")
      If (question("Exit?", "Yes", "No") = True) Then End
    EndIf  
  EndIf



If KeyHit(1) 
  If (question("Exit?", "Yes", "No") = True) Then End
EndIf

    ; Delay 60
  
    Flip()
  Wend

  End
End Function

Function runtimemenu()
  message("Welcome to the world of 'Alien Invaders'!")
  maina$(1) = "SAVE GAME"
  maina$(2) = "LOAD GAME"
  maina$(3) = "EXIT"
  mi# = 1
  Local backgrnd = LoadImage("Images/bd01.bmp")
  Local font = LoadFont("Arial", 18, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font

  Delay 120
  GetKey()
  Delay 120
  
  While Not KeyHit(1)
    SetFont font
  GetKey()
  
    RenderWorld()
  
    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)
  

	For i = 1 To 3
        If i = mi#
            Color 255, 128, 0
        Else
            Color 255, 255, 255
        EndIf
        
        Text GraphicsWidth()/2, (GraphicsHeight()/2)+(i * FontHeight() * 1.25), opta$(i), True, True
    Next

    If KeyDown(208) Then mi# = mi# + 1
    If KeyDown(200) Then mi# = mi# - 1

  If mi# < 1 Then mi# = 1
  If mi# > 3 Then mi# = 3
  
  If KeyHit(28)        
    If mi# = 1 Then save_game(enter_text_dialog("Enter name of this save:", 25)+".ais")

    If mi# = 2 Then load_game(enter_text_dialog("Enter name of loaded save:", 25)+".ais")
    
    If (mi# = 3)      
    ;Delay 120
      If (question("Exit?", "Yes", "No") = True) Then End
    EndIf  
  EndIf    
  
  Flip()
  Wend

  SetFont font0
End Function

Function options_menu()  
  Local backgrnd = LoadImage("Images/bd01.bmp")
  Local font = LoadFont("Arial", 18, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font

  opta$(1) = "GAME"
  opta$(2) = "SOUND"
  opta$(3) = "GRAPHICS"
  opta$(4) = "BACK"
  
  mi# = 1
  
  While Not KeyHit(1)
    SetFont font
    RenderWorld()

    GetKey()
  
    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)

    For i = 1 To 4
        If i = mi#
            Color 255, 128, 0
        Else
            Color 255, 255, 255
        EndIf
        
        Text GraphicsWidth()/2, (GraphicsHeight()/2)+(i * FontHeight() * 1.25), opta$(i), True, True
    Next

    If KeyDown(208) Then mi# = mi# + 1
    If KeyDown(200) Then mi# = mi# - 1

  If mi# < 1 Then mi# = 1
  If mi# > 4 Then mi# = 4
  
  If KeyHit(28)
    If mi# = 1 Then game_options_menu()
    If mi# = 2 Then sound_options_menu()
    If mi# = 3 Then Graphics_options_menu()
    If mi# = 4
      f = ReadFile("Config.ini")
          w=Int(ReadLine(f))
          h=Int(ReadLine(f))
          d=Int(ReadLine(f))
          mde=Int(ReadLine(f))
          fs=Float(ReadLine(f))
          playername$ = ReadLine(f)      
      snd_enable = Int(ReadLine(f))
        CloseFile(f)
    
    f = WriteFile("Config.ini")
      WriteLine(f, Str(w))
      WriteLine(f, Str(h))
      WriteLine(f, Str(d))
      WriteLine(f, Str(mde))
      WriteLine(f, Str(fs))
      WriteLine(f, playername$)
      WriteLine(f, Str(snd_enable))
    CloseFile(f)
      Return
    EndIf  
  EndIf

    Delay 60
  
  Flip()
  Wend
End Function

Function sound_options_menu()  
  Local backgrnd = LoadImage("Images/bd01.bmp")
  Local font = LoadFont("Arial", 18, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font

  opta$(1) = "ENABLE"
  opta$(2) = "DISABLE"  
  opta$(3) = "BACK"
  
  mi# = 1
  
  While Not KeyHit(1)
    SetFont font
    RenderWorld()

  GetKey()
  
    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)


	For i = 1 To 3
        If i = mi#
            Color 255, 128, 0
        Else
            Color 255, 255, 255
        EndIf
        
        Text GraphicsWidth()/2, (GraphicsHeight()/2)+(i * FontHeight() * 1.25), opta$(i), True, True
    Next

    If KeyDown(208) Then mi# = mi# + 1
    If KeyDown(200) Then mi# = mi# - 1

  If mi# < 1 Then mi# = 1
  If mi# > 3 Then mi# = 3
  
  If KeyHit(28)    
    If mi# = 1
      snd_enable = 1
    f = ReadFile("Config.ini")
          w=Int(ReadLine(f))
          h=Int(ReadLine(f))
          d=Int(ReadLine(f))
          mde=Int(ReadLine(f))
          fs=Float(ReadLine(f))
          playername$ = ReadLine(f)      
      snd_enable = Int(ReadLine(f))
        CloseFile(f)
    
    f = WriteFile("Config.ini")
      WriteLine(f, Str(w))
      WriteLine(f, Str(h))
      WriteLine(f, Str(d))
      WriteLine(f, Str(mde))
      WriteLine(f, Str(fs))
      WriteLine(f, playername$)
      WriteLine(f, Str(snd_enable))
    CloseFile(f)
    EndIf
    
    If mi# = 2
      snd_enable = 0
    f = ReadFile("Config.ini")
          w=Int(ReadLine(f))
          h=Int(ReadLine(f))
          d=Int(ReadLine(f))
          mde=Int(ReadLine(f))
          fs=Float(ReadLine(f))
          playername$ = ReadLine(f)      
      snd_enable = Int(ReadLine(f))
        CloseFile(f)
    
    f = WriteFile("Config.ini")
      WriteLine(f, Str(w))
      WriteLine(f, Str(h))
      WriteLine(f, Str(d))
      WriteLine(f, Str(mde))
      WriteLine(f, Str(fs))
      WriteLine(f, playername$)
      WriteLine(f, Str(snd_enable))
    CloseFile(f)
    EndIf
    
    If mi# = 3 Then GetKey() : Return
  EndIf
  
    Delay 60
  
  Flip()
  Wend
End Function

Function Graphics_options_menu()    
  Local backgrnd = LoadImage("Images/bd01.bmp")
  Local font = LoadFont("Arial", 18, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font

  optsag$(1) = "RESOLUTION"  
  optsag$(2) = "DRIVER"
  optsag$(3) = "BACK"
  
  mi# = 1
  
  While Not KeyHit(1)
    SetFont font
    RenderWorld()
  GetKey()
  
    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)


	For i = 1 To 3
        If i = mi#
            Color 255, 128, 0
        Else
            Color 255, 255, 255
        EndIf
        
        Text GraphicsWidth()/2, (GraphicsHeight()/2)+(i * FontHeight() * 1.25), optsag$(i), True, True
    Next

    If KeyDown(208) Then mi# = mi# + 1
    If KeyDown(200) Then mi# = mi# - 1

  If mi# < 1 Then mi# = 1
  If mi# > 3 Then mi# = 3
  
  If KeyHit(28)
    If mi# = 1 Then resolution_options_menu()    
    If mi# = 2 Then graphic_driver_menu()
    If mi# = 3 Then Return
  EndIf

    Delay 60
  
  Flip()
  Wend
End Function

Function graphic_driver_menu()
  kx# = CountGfxDrivers()
  
  For i = 1 To kx#
    optsad$(i) = GfxDriverName$(i)
  Next
  
  optsad$(kx# + 1) = "BACK"
  
  mi# = 1
  
  Local backgrnd = LoadImage("Images/bd01.bmp")
  Local font = LoadFont("Arial", 18, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font
  
  While Not KeyHit(1)
    SetFont font
    RenderWorld()

  GetKey()
  
    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)


	For i = 1 To kx# + 1
        If i = mi#
            Color 255, 128, 0
        Else
            Color 255, 255, 255
        EndIf
        
        Text GraphicsWidth()/2, (GraphicsHeight()/2)+(i * FontHeight() * 1.25), optsad$(i), True, True
    Next

    If KeyDown(208) Then mi# = mi# + 1
    If KeyDown(200) Then mi# = mi# - 1

  If mi# < 1 Then mi# = 1
  If mi# > kx# + 1 Then mi# = kx# + 1
  
  If KeyHit(28)    
    If mi# = kx# + 1 Then Return Else
      SetGfxDriver mi#
  EndIf

    Delay 60
  
  Flip()
  Wend
End Function

Function resolution_options_menu()  
  optsar$(1) = "600*800*16"
  optsar$(2) = "600*800*32"
  optsar$(3) = "1024*768*16"
  optsar$(4) = "1024*768*32"
  optsar$(5) = "1280*1024*16"
  optsar$(6) = "1280*1024*32"
  optsar$(7) = "BACK"
  
  mi# = 1
  
  Local backgrnd = LoadImage("Images/bd01.bmp")
  Local font = LoadFont("Arial", 18, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font
  
  While Not KeyHit(1)
    SetFont font
    RenderWorld()

  GetKey()
  
    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)


	For i = 1 To 7
        If i = mi#
            Color 255, 128, 0
        Else
            Color 255, 255, 255
        EndIf
        
        Text GraphicsWidth()/2, (GraphicsHeight()/2)+(i * FontHeight() * 1.25), optsar$(i), True, True
    Next

    If KeyDown(208) Then mi# = mi# + 1
    If KeyDown(200) Then mi# = mi# - 1

  If mi# < 1 Then mi# = 1
  If mi# > 7 Then mi# = 7
  
  If KeyHit(28)
    If mi# = 1
      f = ReadFile("Config.ini")
      w=Int(ReadLine(f))
          h=Int(ReadLine(f))
          d=Int(ReadLine(f))
          mde=Int(ReadLine(f))
          fs=Float(ReadLine(f))
      playername$ = ReadLine(f)
      snd_enable = Int(ReadLine(f))
    CloseFile(f)

    f = WriteFile("Config.ini")
      WriteLine(f, "600")
      WriteLine(f, "800")
      WriteLine(f, "16")
      WriteLine(f, Str(mde))
      WriteLine(f, Str(fs))
      WriteLine(f, playername$)
      WriteLine(f, Str(snd_enable))
    CloseFile(f)
    EndIf

    If mi# = 2
      f = ReadFile("Config.ini")
      w=Int(ReadLine(f))
          h=Int(ReadLine(f))
          d=Int(ReadLine(f))
          mde=Int(ReadLine(f))
          fs=Float(ReadLine(f))
      playername$ = ReadLine(f)
      snd_enable = Int(ReadLine(f))
    CloseFile(f)

    f = WriteFile("Config.ini")
      WriteLine(f, "600")
      WriteLine(f, "800")
      WriteLine(f, "32")
      WriteLine(f, Str(mde))
      WriteLine(f, Str(fs))
      WriteLine(f, playername$)
      WriteLine(f, Str(snd_enable))
    CloseFile(f)
    EndIf

    If mi# = 3
      f = ReadFile("Config.ini")
      w=Int(ReadLine(f))
          h=Int(ReadLine(f))
          d=Int(ReadLine(f))
          mde=Int(ReadLine(f))
          fs=Float(ReadLine(f))
      playername$ = ReadLine(f)
      snd_enable = Int(ReadLine(f))
    CloseFile(f)

    f = WriteFile("Config.ini")
      WriteLine(f, "1024")
      WriteLine(f, "768")
      WriteLine(f, "16")
      WriteLine(f, Str(mde))
      WriteLine(f, Str(fs))
      WriteLine(f, playername$)
      WriteLine(f, Str(snd_enable))
    CloseFile(f)
    EndIf

    If mi# = 4
      f = ReadFile("Config.ini")
      w=Int(ReadLine(f))
          h=Int(ReadLine(f))
          d=Int(ReadLine(f))
          mde=Int(ReadLine(f))
          fs=Float(ReadLine(f))
      playername$ = ReadLine(f)
      snd_enable = Int(ReadLine(f))
    CloseFile(f)

    f = WriteFile("Config.ini")
      WriteLine(f, "1024")
      WriteLine(f, "768")
      WriteLine(f, "32")
      WriteLine(f, Str(mde))
      WriteLine(f, Str(fs))
      WriteLine(f, playername$)
      WriteLine(f, Str(snd_enable))
    CloseFile(f)
    EndIf

    If mi# = 5
      f = ReadFile("Config.ini")
      w=Int(ReadLine(f))
          h=Int(ReadLine(f))
          d=Int(ReadLine(f))
          mde=Int(ReadLine(f))
          fs=Float(ReadLine(f))
      playername$ = ReadLine(f)
      snd_enable = Int(ReadLine(f))
    CloseFile(f)

    f = WriteFile("Config.ini")
      WriteLine(f, "1280")
      WriteLine(f, "1024")
      WriteLine(f, "16")
      WriteLine(f, Str(mde))
      WriteLine(f, Str(fs))
      WriteLine(f, playername$)
      WriteLine(f, Str(snd_enable))
    CloseFile(f)
    EndIf

    If mi# = 6
      f = ReadFile("Config.ini")
      w=Int(ReadLine(f))
          h=Int(ReadLine(f))
          d=Int(ReadLine(f))
          mde=Int(ReadLine(f))
          fs=Float(ReadLine(f))
      playername$ = ReadLine(f)
      snd_enable = Int(ReadLine(f))
    CloseFile(f)

    f = WriteFile("Config.ini")
      WriteLine(f, "1280")
      WriteLine(f, "1024")
      WriteLine(f, "32")
      WriteLine(f, Str(mde))
      WriteLine(f, Str(fs))
      WriteLine(f, playername$)
      WriteLine(f, Str(snd_enable))
    CloseFile(f)
    EndIf
    
    If mi# = 7 Then message("You will need to restart game!") : Return
  EndIf

    Delay 60
  
  Flip()
  Wend
End Function

Function game_options_menu()
  optsgoa$(1) = "PLAYER NAME"
  optsgoa$(2) = "BACK"
  
  Local backgrnd = LoadImage("Images/bd01.bmp")
  Local font = LoadFont("Arial", 18, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font
  
  While Not KeyHit(1)
    SetFont font
    RenderWorld()

    GetKey()
  
    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)


	For i = 1 To 2
        If i = mi#
            Color 255, 128, 0
        Else
            Color 255, 255, 255
        EndIf
        
        Text GraphicsWidth()/2, (GraphicsHeight()/2)+(i * FontHeight() * 1.25), optsgoa$(i), True, True
    Next

    If KeyDown(208) Then mi# = mi# + 1
    If KeyDown(200) Then mi# = mi# - 1

  If mi# < 1 Then mi# = 1
  If mi# > 2 Then mi# = 2
  
  If KeyHit(28)
    If mi# = 1 Then playername$ = enter_text_dialog$("Enter player's name (under 25 chars):", 25)
    If mi# = 2 Then Return
  EndIf

    Delay 60
  
  Flip()
  Wend
End Function

Function enter_text_dialog$(msg$, symbols#)
  Local backgrnd = LoadImage("Images/bd01.bmp")
  Local font = LoadFont("Arial", 18, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font
  
  s$ = ""
  kz# = 0
  
  While (kz# <= symbols#)
    SetFont font
    RenderWorld()
  
    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)
  Color 255,128,0
  Text GraphicsWidth()/2 - (StringWidth(msg$)/2), GraphicsHeight()/2, msg$
  Text GraphicsWidth()/2 - (StringWidth(s$)/2), GraphicsHeight()/2+20, s$

    value = GetKey()
  
  If (value = 8) And (kz# >= 1)
    kz# = kz# - 1
    s$ = Left(s$, kz#)
  EndIf

  If value = 27 Then Return ""
  
  If (value >= 33) And (value <= 126)
    s$ = s$ + Chr(value)
    kz# = kz# + 1
  EndIf    
  
  If KeyHit(28)
    Return s$
  EndIf

    Delay 60
  
  Flip()
  Wend
End Function

Function save_game(savename$)
  f = WriteFile(savename$)
    WriteLine(f, Str(user\x#))
    WriteLine(f, Str(user\y#))
    WriteLine(f, Str(user\z#))
    WriteLine(f, Str(user\xa#))
    WriteLine(f, Str(user\ya#))
    WriteLine(f, Str(user\za#))
    WriteLine(f, Str(user\MaxSpd#))
    WriteLine(f, Str(user\HP#))
    WriteLine(f, Str(user\Armor#))
    WriteLine(f, Str(user\ProtectionR#))
    WriteLine(f, Str(user\Damage#))
    WriteLine(f, Str(user\PickR#))

  WriteLine(f, Str(PlanetsCnt))
  
    For i=1 To PlanetsCnt    
    WriteLine(f, Str(Planet[i]\Name$))
    WriteLine(f, Str(Planet[i]\x#))
    WriteLine(f, Str(Planet[i]\y#))
    WriteLine(f, Str(Planet[i]\z#))
    WriteLine(f, Str(Planet[i]\PScale#))
    WriteLine(f, Str(Planet[i]\Width#))
    WriteLine(f, Str(Planet[i]\Height#))
    WriteLine(f, Str(Planet[i]\Depth#))
    WriteLine(f, Str(Planet[i]\Xa#))
    WriteLine(f, Str(Planet[i]\Ya#))
    WriteLine(f, Str(Planet[i]\Za#))
    WriteLine(f, Str(Planet[i]\Money#))
  Next

    WriteLine(f, Str(BotCnt))

  For i = 1 To BotCnt    
    WriteLine(f, Str(Bot[i]\Name$))
    WriteLine(f, Str(Bot[i]\X#))
    WriteLine(f, Str(Bot[i]\Y#))
    WriteLine(f, Str(Bot[i]\Z#))
    
    WriteLine(f, Str(Bot[i]\Xa#))
    WriteLine(f, Str(Bot[i]\Ya#))
    WriteLine(f, Str(Bot[i]\Za#))
    
    WriteLine(f, Str(Bot[i]\Xt#))
    WriteLine(f, Str(Bot[i]\Yt#))
    WriteLine(f, Str(Bot[i]\Zt#))
    
    WriteLine(f, Str(Bot[i]\ViewR#))
    WriteLine(f, Str(Bot[i]\AttackR#))
    WriteLine(f, Str(Bot[i]\RunR#))

    WriteLine(f, Str(Bot[i]\HP#))
      WriteLine(f, Str(Bot[i]\Armor#))
      WriteLine(f, Str(Bot[i]\Weapon$))
      WriteLine(f, Str(Bot[i]\Ammo#))
      WriteLine(f, Str(Bot[i]\Damage#))
    WriteLine(f, Str(Bot[i]\Accuracy#))
      WriteLine(f, Str(Bot[i]\InventoryNum#))
    WriteLine(f, Str(Bot[i]\ItemsCnt#))

    For t = 1 To Bot[i]\ItemsCnt#
      WriteLine(f, Str(BotItems(i, t)))
    WriteLine(f, Str(BotItems(i, t)\SValue0$))
    Next

    WriteLine(f, Str(Bot[i]\Character#))
    WriteLine(f, Str(Bot[i]\BotType#))
    WriteLine(f, Str(Bot[i]\Race#))

    WriteLine(f, Str(Bot[i]\TargetOk#))
    WriteLine(f, Str(Bot[i]\Target))
    WriteLine(f, Str(Bot[i]\Dead#))
    WriteLine(f, Str(Bot[i]\Money#))
  Next

    WriteLine(f, Str(InventoryItemCnt))
  
  For i = 1 To InventoryItemCnt      
    WriteLine(f, Str(Inventory[i]))
    Next

  For i = 1 To BotCnt
      For t = 1 To Bot[i]\ItemsCnt#              
    WriteLine(f, Str(BotItems(i, t)\SValue0$))
      WriteLine(f, Str(BotItems(i, t)\SValue1$))
      WriteLine(f, Str(BotItems(i, t)\FValue0#))
      WriteLine(f, Str(BotItems(i, t)\SValue2$))
      WriteLine(f, Str(BotItems(i, t)\FValue1#))          
    Next
  Next

  WriteLine(f, Str(QuestCnt))

    For i = 1 To QuestCnt      
    WriteLine(f, Str(Quest[i]\GiverType#))
    WriteLine(f, Str(Quest[i]\Giver$))
    WriteLine(f, Str(Quest[i]\Condition#))

    k# = 0
    
        If Quest[i]\SValue0$ <> "" Then k# = k# + 1
    If Quest[i]\SValue1$ <> "" Then k# = k# + 1
    If Quest[i]\SValue2$ <> "" Then k# = k# + 1
    If Quest[i]\SValue3$ <> "" Then k# = k# + 1
    If Quest[i]\SValue4$ <> "" Then k# = k# + 1
    If Quest[i]\SValue5$ <> "" Then k# = k# + 1

    WriteLine(f, Str(k#))
    
    If k# >= 1 Then WriteLine(f, Str(Quest[i]\SValue0$))
    If k# >= 2 Then WriteLine(f, Str(Quest[i]\SValue1$))
    If k# >= 3 Then WriteLine(f, Str(Quest[i]\SValue2$))
    If k# >= 4 Then WriteLine(f, Str(Quest[i]\SValue3$))
    If k# >= 5 Then WriteLine(f, Str(Quest[i]\SValue4$))
    If k# = 6 Then WriteLine(f, Str(Quest[i]\SValue5$))

    k# = 0
        
    If Quest[i]\FValue0# <> 0 Then k# = k# + 1
    If Quest[i]\FValue1# <> 0 Then k# = k# + 1
    If Quest[i]\FValue2# <> 0 Then k# = k# + 1
    If Quest[i]\FValue3# <> 0 Then k# = k# + 1
    If Quest[i]\FValue4# <> 0 Then k# = k# + 1
    If Quest[i]\FValue5# <> 0 Then k# = k# + 1    

    WriteLine(f, Str(k#))
    
    If k# >= 1 Then WriteLine(f, Str(Quest[i]\FValue0#))
    If k# >= 2 Then WriteLine(f, Str(Quest[i]\FValue1#))
    If k# >= 3 Then WriteLine(f, Str(Quest[i]\FValue2#))
    If k# >= 4 Then WriteLine(f, Str(Quest[i]\FValue3#))
    If k# >= 5 Then WriteLine(f, Str(Quest[i]\FValue4#))
    If k# = 6 Then WriteLine(f, Str(Quest[i]\FValue5#))

    WriteLine(f, Str(Quest[i]\Active#))
    WriteLine(f, Str(Quest[i]\Done#))
    Next
  CloseFile(f)
End Function

Function load_game(savename$)
f = ReadFile(savename$)
If (savename$ <> "") And (f <> 0)
  CloseFile(f)
  If user = Null Then user = New TPlayer
  If user\Entity = 0 Then user\Entity = CreateEntity()
  f = ReadFile(savename$)
    user\x# = Float(ReadLine(f))
    user\y# = Float(ReadLine(f))
    user\z# = Float(ReadLine(f))
    user\xa# = Float(ReadLine(f))
    user\ya# = Float(ReadLine(f))
    user\za# = Float(ReadLine(f))
    user\MaxSpd# = Float(ReadLine(f))
    user\HP# = Float(ReadLine(f))
    user\Armor# = Float(ReadLine(f))
    user\ProtectionR# = Float(ReadLine(f))
    user\Damage# = Float(ReadLine(f))
    user\PickR# = Float(ReadLine(f))
    EntityType user\Entity, USERT
    PositionEntity user\Entity, user\x#, user\y#, user\z#
    RotateEntity user\Entity, user\xa#, user\ya#, user\za#
    EntityColor user\Entity, 5, 5, 145
    EntityPickMode user\Entity, 1

    If camera = 0 Then camera = CreateCamera()
    PositionEntity camera, user\x#, user\y#+2.5, user\z#-5  
    EntityParent camera, user\Entity
    CameraRange camera, 0.1, 1000

    If light = 0 Then light = CreateLight()
    LightColor light, 255, 255, 255
    PositionEntity light, 0, 25, 0

    sky = CreateEntity()
    ScaleEntity sky, 5000, 5000, 5000
    FlipMesh sky
    tex = LoadTexture("Images/backdrop.bmp")
    EntityTexture sky, tex
    
    PlanetsCnt = Int(ReadLine(f))
  
    For i=1 To PlanetsCnt
    If Planet[i] = Null Then Planet[i] = New TPlanet
    
      Planet[i]\Name$ = ReadLine(f)    
    Planet[i]\x# = Float(ReadLine(f))
    Planet[i]\y# = Float(ReadLine(f))
    Planet[i]\z# = Float(ReadLine(f))  
    Planet[i]\PScale# = Float(ReadLine(f))
    Planet[i]\Width# = Float(ReadLine(f))
    Planet[i]\Height# = Float(ReadLine(f))
    Planet[i]\Depth# = Float(ReadLine(f))
    Planet[i]\Xa# = Float(ReadLine(f))
    Planet[i]\Ya# = Float(ReadLine(f))
    Planet[i]\Za# = Float(ReadLine(f))
    Planet[i]\Money# = Float(ReadLine(f))

    If Planet[i]\Entity = 0 Then Planet[i]\Entity = CreateEntity()
    tex = LoadTexture("Images/earth.bmp")
    EntityTexture Planet[i]\Entity, tex
    FreeTexture tex
    EntityPickMode Planet[i]\Entity, 2
    ScaleEntity Planet[i]\Entity, Planet[i]\PScale#, Planet[i]\PScale#, Planet[i]\PScale#
    PositionEntity Planet[i]\Entity, Planet[i]\X#, Planet[i]\Y#, Planet[i]\Z#    
    Next
    
    BotCnt = Int(ReadLine(f))

  For i = 1 To BotCnt
    If Bot[i] = Null Then Bot[i] = New TBot    

    Bot[i]\Name$ = ReadLine(f)
    Bot[i]\X# = Float(ReadLine(f))
    Bot[i]\Y# = Float(ReadLine(f))
    Bot[i]\Z# = Float(ReadLine(f))
    
    Bot[i]\Xa# = Float(ReadLine(f))
    Bot[i]\Ya# = Float(ReadLine(f))
    Bot[i]\Za# = Float(ReadLine(f))
    
    Bot[i]\Xt# = Float(ReadLine(f))
    Bot[i]\Yt# = Float(ReadLine(f))
    Bot[i]\Zt# = Float(ReadLine(f))
    
    Bot[i]\ViewR# = Float(ReadLine(f))
    Bot[i]\AttackR# = Float(ReadLine(f))
    Bot[i]\RunR# = Float(ReadLine(f))

    Bot[i]\HP# = Float(ReadLine(f))
      Bot[i]\Armor# = Float(ReadLine(f))
      Bot[i]\Weapon$ = ReadLine(f)
      Bot[i]\Ammo# = Float(ReadLine(f))
      Bot[i]\Damage# = Float(ReadLine(f))
    Bot[i]\Accuracy# = Float(ReadLine(f))
      Bot[i]\InventoryNum# = Float(ReadLine(f))
    Bot[i]\ItemsCnt# = Float(ReadLine(f))

    For t = 1 To Bot[i]\ItemsCnt#
      If BotItems(i, t) = Null Then BotItems(i, t) = New TItem
    BotItems(i, t)\SValue0$ = ReadLine(f)       
    Next

    Bot[i]\Character# = Float(ReadLine(f))
    Bot[i]\BotType# = Float(ReadLine(f))
    Bot[i]\Race# = Float(ReadLine(f))

    If Bot[i]\Entity = 0 Then Bot[i]\Entity = CreateEntity()

    Bot[i]\TargetOk# = Float(ReadLine(f))
    Bot[i]\Target = Float(ReadLine(f))
    Bot[i]\Dead# = Float(ReadLine(f))
    Bot[i]\Money# = Float(ReadLine(f))

    EntityType Bot[i]\Entity, BOTT
    RotateEntity Bot[i]\Entity, Bot[i]\Xa#, Bot[i]\Ya#, Bot[i]\Za#
    PositionEntity Bot[i]\Entity, Bot[i]\X#, Bot[i]\Y#, Bot[i]\Z#
    EntityPickMode Bot[i]\Entity, 1

    If Bot[i]\Character# < 25 Then EntityColor Bot[i]\Entity, 255, 0, 0 Else
    If (Bot[i]\Character# >= 25) And (Bot[i]\Character# <= 75) Then EntityColor Bot[i]\Entity, 255,255,0 Else
    If Bot[i]\Character# > 75 Then EntityColor Bot[i]\Entity, 0, 255, 0 Else
  Next
  
    ItemCnt = Int(ReadLine(f))

  For i = 1 To ItemCnt
    If Item[i] = Null Then Item[i] = New TItem
    
    Item[i]\SValue0$ = ReadLine(f)
    Item[i]\SValue1$ = ReadLine(f)
    Item[i]\FValue0# = Float(ReadLine(f))
    Item[i]\SValue2$ = ReadLine(f)
    Item[i]\FValue1# = Float(ReadLine(f))
  Next

  For i = 1 To 5
    InventoryItemCnt = i
  If Inventory[i] = Null Then Inventory[i] = New TItem
  Inventory[i] = Item[Rnd(ItemCnt)+1]
  Next

  For i = 1 To BotCnt
      For t = 1 To Bot[i]\ItemsCnt#      
        For j = 1 To ItemCnt
      If Item[j]\SValue0$ = BotItems(i, t)\SValue0$
        BotItems(i, t)\SValue0$ = Item[j]\SValue0$
          BotItems(i, t)\SValue1$ = Item[j]\SValue1$
          BotItems(i, t)\FValue0# = Item[j]\FValue0#
          BotItems(i, t)\SValue2$ = Item[j]\SValue2$
          BotItems(i, t)\FValue1# = Item[j]\FValue1#

      If Item[j]\SValue1$ = "Protection" Then Bot[i]\Armor# = Bot[i]\Armor# + Item[j]\FValue0#
      If Item[j]\SValue1$ = "Speed" Then Bot[i]\MaxSpeed# = Bot[i]\MaxSpeed# + Item[j]\FValue0#
      If Item[j]\SValue1$ = "View" Then Bot[i]\ViewR# = Bot[i]\ViewR# + Item[j]\FValue0#
      If Item[j]\SValue1$ = "Damage" Then Bot[i]\Damage# = Bot[i]\Damage# + Item[j]\FValue0#
      If Item[j]\SValue1$ = "Count" Then Bot[i]\Ammo# = Bot[i]\Ammo# + Item[j]\FValue0#
      
        Exit
      EndIf  
    Next  
    Next
  Next
  
    QuestCnt = Int(ReadLine(f))

    For i = 1 To QuestCnt
      If Quest[i] = Null Then Quest[i] = New TQuest        
    Quest[i]\GiverType# = Float(ReadLine(f))    
    Quest[i]\Giver$ = ReadLine(f)
    Quest[i]\Condition# = Float(ReadLine(f))

    k# = Float(ReadLine(f))    
    If k# >= 1 Then Quest[i]\SValue0$ = ReadLine(f)
    If k# >= 2 Then Quest[i]\SValue1$ = ReadLine(f)
    If k# >= 3 Then Quest[i]\SValue2$ = ReadLine(f)
    If k# >= 4 Then Quest[i]\SValue3$ = ReadLine(f)
    If k# >= 5 Then Quest[i]\SValue4$ = ReadLine(f)
    If k# = 6 Then Quest[i]\SValue5$ = ReadLine(f)

    k# = Float(ReadLine(f))
    If k# >= 1 Then Quest[i]\FValue0# = Float(ReadLine(f))
    If k# >= 2 Then Quest[i]\FValue1# = Float(ReadLine(f))
    If k# >= 3 Then Quest[i]\FValue2# = Float(ReadLine(f))
    If k# >= 4 Then Quest[i]\FValue3# = Float(ReadLine(f))
    If k# >= 5 Then Quest[i]\FValue4# = Float(ReadLine(f))
    If k# = 6 Then Quest[i]\FValue5# = Float(ReadLine(f))

    Quest[i]\Active# = Float(ReadLine(f))
    Quest[i]\Done# = Float(ReadLine(f))
    Next
  CloseFile(f)

  update_user_params()
Else
  message("Save doesn't exists!")
EndIf  
End Function

Function CreateEntity()
  Local hndl
  
  hndl = 0

  While hndl = 0
    hndl = CreateSphere()
  Wend

  Return hndl
End Function

Function ShockIndicator(stx#, sty#, ndx#, ndy#, xstep#, ystep#) ;StartX, StartY, EndX, EndY, StepX, StepY
  ;===INDICATOR SPECEFFECT===
  Local st, nd, a, i, oa, oi
  
  st = stx+(Rnd(xstep)+1)*ystep
  SeedRnd MilliSecs()
  nd = st+((Rnd(xstep)+1)*ystep)
  Color 20, 115, 105
  If nd > 100 Then nd = 100

  oa = 10
  oi = st
  Line stx, sty, stx, sty+10
  
  Color 255-(user\HP#*2.55), 0, 0
  
  For i = st To nd Step 2
    a = (stx - 2)+Rnd(xstep+2)
  Line oa, oi, a, i
  oa = a
  oi = i
  Next

  Color 20, 115, 105  
  Line ndx, ndy, ndx, ndy+10
  Text stx#, (ndy#-sty#)/2, Str(Int(user\HP#))
  ;===END INDICATOR===
End Function

Function BotDetector()
  ;====BOT DETECTOR====  
  For i = 1 To BotCnt
    CameraProject(camera,EntityX(Bot[i]\Entity),EntityY(Bot[i]\Entity),EntityZ(Bot[i]\Entity))
  If EntityInView(Bot[i]\Entity, camera)
    If Bot[i]\BotType# < 25 Then Color 255, 150, 75 Else
    If (Bot[i]\BotType# >= 25) And (Bot[i]\BotType# < 50) Then Color 255, 255, 75 Else
    If (Bot[i]\BotType# >= 50) And (Bot[i]\BotType# < 75) Then Color 255, 255, 175 Else
    If (Bot[i]\BotType# >= 75) Then Color 0, 255, 0

      If (Bot[i]\Dead# = 1) Then Color 145,155,155
    s$ = Bot[i]\Name$;+"  aka  "+Bot[i]\HP#;+" :: "+Bot[i]\Character#+" :: "+Bot[i]\BotType#
    Text ProjectedX#()-StringWidth(s$)/2, ProjectedY#(), s$
  EndIf  
  Next  
  ;====END DETECTOR====
End Function

Function itemdetector()  
  For it.TItem = Each TItem
    If it\Entity > 0              
    CameraProject(camera, EntityX(it\Entity),EntityY(it\Entity), EntityZ(it\Entity))
    If EntityInView(it\Entity, camera)
      Color 255, 255, 255      
      Text ProjectedX()-StringWidth(it\SValue0$), ProjectedY(), it\SValue0$
    EndIf
  EndIf
  Next
End Function

Function update_user()
  If KeyDown(200) Then TurnEntity user\Entity, 1, 0, 0  
  If KeyDown(208) Then TurnEntity user\Entity, -1, 0, 0
  If KeyDown(203) Then TurnEntity user\Entity, 0, 0, 1    
  If KeyDown(205) Then TurnEntity user\Entity, 0, 0, -1  
  
  user\Xa# = EntityPitch(user\Entity)
  user\Ya# = EntityYaw(user\Entity)
  user\Za# = EntityRoll(user\Entity)  
  
  If KeyDown(157) 
    If user\Speed# < user\MaxSpd# Then user\Speed# = user\Speed# + 1
  EndIf

  If KeyHit(1)
    runtimemenu()
  EndIf

  If KeyDown(54)
    If user\Speed# > 0 Then user\Speed# = user\Speed# - 1
  EndIf

  If KeyDown(15)
    show_user_params()
  EndIf

  If KeyDown(16)
    show_quests()
  EndIf

  If snd_enable = 1
    If KeyDown(19)    
      If m = 1 Then FreeSound snd1    
    If m = 2 Then FreeSound snd2  
    If m = 3 Then FreeSound snd3
    If m = 3 Then FreeSound snd4
    If m = 3 Then FreeSound snd5

    m = m + 1
    If m > 5 Then m = 1

    If (m = 1)  Then PlaySound snd1
    If (m = 2)  Then PlaySound snd2
    If (m = 3)  Then PlaySound snd3
    If (m = 4)  Then PlaySound snd4
    If (m = 5)  Then PlaySound snd5
    EndIf
  EndIf  

  MoveEntity user\Entity, 0, 0, user\Speed#/10

  PositionEntity sky, EntityX(user\Entity), EntityY(user\Entity), EntityZ(user\Entity)

  user\X# = EntityX(user\Entity)
  user\Y# = EntityY(user\Entity)
  user\Z# = EntityZ(user\Entity)

  If user\HP# <= 0 Then bot_death(user\x#, user\y#, user\z#)

  UpdateWorld()
  RenderWorld()

  ;=============2D OPERATIONS=============  

  ShockIndicator(10, 10, 10, 100, 5, 10)
  BotDetector()
  ItemDetector()
  PickItem()
  Update_Quests()

  ;===ROTATION INDICATOR===
  ;Angle to display, coords of center
  Local STAngle#, X#, Y#
  
  ;===END INDICATOR===

  DrawImage pricel, GraphicsWidth()/2, GraphicsHeight()/2

  If KeyDown(57)
    CameraPick camera, GraphicsWidth()/2, GraphicsHeight()/2
    
  p = PickedEntity()

    Color 200,190,65
    Line GraphicsWidth()/2, GraphicsHeight()-(GraphicsHeight()/3), GraphicsWidth()/2, GraphicsHeight()/2
  
  For z.TBot = Each TBot
    If z\entity = p
      accuracy# = 100-user\Speed#-z\speed#
      z\hp# = z\hp# - (user\Damage#/Abs(z\Armor#))
    z\Armor# = z\Armor# - user\Damage#/10
    EndIf
  Next
  EndIf

  If KeyHit(14)
    CameraPick camera, GraphicsWidth()/2, GraphicsHeight()/2
  p = PickedEntity()

  For z.TBot = Each TBot
    If (z\Sphere = p) Or (z\Entity = p)
      Trade(user\Entity, z\Entity, z)
    Exit
    EndIf
  Next    
  EndIf

  If KeyDown(28)
    CameraPick camera, GraphicsWidth()/2, GraphicsHeight()/2
  p = PickedEntity()

  For pl.TPlanet = Each TPlanet
    If (pl\Entity = p) And (EntityDistance(pl\Entity, user\Entity) <= user\PickR#*10)
      planet_menu(pl)
    Exit
    EndIf  
  Next
  EndIf

  ;==============END 2D================

  Flip()
End Function

Function update_quests()
  For q.TQuest = Each TQuest
    If (q\Active# = 0) And (q\Done# = 0) ;if quest is not active
    If (q\GiverType# = 0) ;and bot gives it
    
      For b.TBot = Each TBot ;searching for this bot
      If (b\Name$ = q\Giver$)
        If (EntityDistance(user\Entity, b\Entity) <= b\ViewR#) ;We've found him!
        q\Active# = 1
        message("<Radio> New quest: "+Left(q\SValue0$, 18)+"... (press <Esc> , <Q>)")
          Exit
      EndIf
      EndIf
    Next        
    EndIf

  ElseIf (q\Active# = 1) And (q\Done# = 0) ;If it's active, but haven't been done
    If (q\Condition# = 1) ;If we must be at (X, Y, Z)+/-R
      If (pvt = 0) Then pvt = CreatePivot()
    PositionEntity pvt, q\FValue1#, q\FValue2#, q\FValue3#

    If (EntityDistance(user\Entity, pvt) <= q\FValue4#) ;Quest done!
      q\Active# = 0
      q\Done# = 1
      message(q\SValue0$+": quest done")
    EndIf
    ElseIf (q\Condition# = 2) ;If we must bring smthng here
      If (q\GiverType# = 0) ;If giver is a bot
      ;Searching for him!
      For b.TBot = Each TBot
        If (q\Giver$ = b\Name$) ;If he's found
        If (EntityDistance(user\Entity, b\Entity) <= q\FValue5#) ;And he is rather near
          q\Active# = 0
        q\Done# = 1
        message(q\SValue0$+": quest done")
        EndIf
      EndIf
      Next      
    EndIf

    ElseIf (q\Condition# = 3) ;If we must bring smthng to smbd
      If (q\FValue0# = 0) ;Bring it to bot
      For b.TBot = Each TBot ;Searching for this bot
        If (b\Name$ = q\SValue2$) And (EntityDistance(b\Entity, user\Entity) <= user\PickR#)
        q\Active# = 0
        q\Done# = 1
        message(q\SValue0$+": quest done")
        Exit
      EndIf
      Next    
    EndIf
    EndIf
  EndIf
  Next
End Function

Function update_user_params()
  For j = 1 To InventoryItemCnt
    If Inventory[j]\SValue1$ = "Protection" Then user\Armor# = user\Armor# + Inventory[j]\FValue0#
  If Inventory[j]\SValue1$ = "Speed" Then user\MaxSpd# = user\MaxSpd# + Inventory[j]\FValue0#
  If Inventory[j]\SValue1$ = "View" Then user\PickR# = user\PickR# + Inventory[j]\FValue0#
  If Inventory[j]\SValue1$ = "Damage" Then user\Damage# = user\Damage# + Inventory[j]\FValue0#
  If Inventory[j]\SValue1$ = "Count" Then user\Ammo# = user\Ammo# + Inventory[j]\FValue0#
  Next  
End Function

Function trade(initier#, target#, b.TBot)
If (b\Race < 60) And (b\Character > 25)  
  Local backgrnd = LoadImage("Images/trade1.bmp")
  Local cursor = LoadImage("Images/tradecursor.bmp")
  Local cx#, cy#, ix#, iy#, sty1#, ndy1#, sty2#, ndy2#, Mode#

  Mode# = 0;Buy

  cx# = GraphicsWidth()/2 - (ImageWidth(backgrnd)/2)
  
  MaskImage backgrnd, 0, 0, 0
  MaskImage cursor, 0, 0, 0
  
  While Not KeyHit(1)
    RenderWorld()

    ;==TRADE==  
  DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)

  y1 = GraphicsHeight()/2 - (ImageHeight(backgrnd)/2) + 10
  y2 = GraphicsHeight()/2 + (ImageHeight(backgrnd)/2) - 10
  For y = y1 To y1+(b\ItemsCnt#*15) Step 15
    nn = ((y-y1)/15)+1
      If nn <= b\ItemsCnt#
      s$ = "<" + b\name$ + ">  " + BotItems(b\InventoryNum, nn)\SValue0$ + "     " + BotItems(b\InventoryNum, nn)\SValue1$ + "=" 
    s$ = s$ + BotItems(b\InventoryNum, nn)\FValue0# + "    Price:" + BotItems(b\InventoryNum, nn)\FValue1#
    Color 240,215,20
      Text GraphicsWidth()/2 - (ImageWidth(backgrnd)/2) + 10, y, s$
    EndIf      
  Next

    ys# = y1+(b\ItemsCnt#*15)+15
  For y = ys# To y2 Step 15
    nn = ((y-ys#)/15)+1
    If nn <= InventoryItemCnt
      s$ = "<Player>   "+Inventory[nn]\SValue0$+"     "+Inventory[nn]\SValue1$+"   "+Inventory[nn]\FValue0#+"   Price:"+Inventory[nn]\FValue1#
    Color 240,215,20
      Text GraphicsWidth()/2 - (ImageWidth(backgrnd)/2) + 10, y, s$    
    EndIf
  Next  

  If KeyDown(200) Then iy# = iy# - 1
  If KeyDown(208) Then iy# = iy# + 1
  If KeyDown(15) Then Mode# = Not Mode#

  sty1# = 0
  ndy1# = b\ItemsCnt#-1
  sty2# = ndy1#+2
  ndy2# = sty2#+InventoryItemCnt-1
  
  If KeyHit(28)
    If Mode# = 0
      If (user\money# >= BotItems(b\InventoryNum, iy#+1)\FValue1#) And (question("Buy "+BotItems(b\InventoryNum, iy#+1)\SValue0$+" for "+BotItems(b\InventoryNum, iy#+1)\FValue1#+" lv?", "Yes", "No") = True)
        user\money# = user\money# - BotItems(b\InventoryNum, iy#+1)\FValue1#
      b\money# = b\money# + BotItems(b\InventoryNum, iy#+1)\FValue1#
      InventoryItemCnt = InventoryItemCnt + 1
      Inventory[InventoryItemCnt] = New TItem
      Inventory[InventoryItemCnt] = BotItems(b\InventoryNum, iy#+1)

      For i = iy#+1 To b\ItemsCnt-1      
      BotItems(b\InventoryNum, i) = BotItems(b\InventoryNum, i+1)
      Next  
      b\ItemsCnt = b\ItemsCnt - 1
      EndIf

    update_user_params()
    Else
      If (b\money# >= Inventory[iy#-b\ItemsCnt]\FValue1#) And (question("Sell "+Inventory[iy#-b\ItemsCnt]\SValue0$+" for "+Inventory[iy#-b\ItemsCnt]\FValue1#+" lv?", "Yes", "No") = True)
        b\money# = b\money# - Inventory[iy#-b\ItemsCnt]\FValue1#
      b\ItemsCnt = b\ItemsCnt + 1
      BotItems(b\InventoryNum, b\ItemsCnt) = New TItem
      BotItems(b\InventoryNum, b\ItemsCnt) = Inventory[iy#+1-b\ItemsCnt]
      user\Money# = user\Money# + Inventory[iy#+1-b\ItemsCnt]\FValue1#
            
      For i = iy#+1-b\ItemsCnt To InventoryItemCnt - 1
        Inventory[i] = Inventory[i+1]
      Next
      InventoryItemCnt = InventoryItemCnt - 1
      EndIf
    EndIf

    update_user_params()
  EndIf

    If Mode# = 0    
    If iy# < sty1# Then iy# = sty1#
    If iy# > ndy1# Then iy# = ndy1#
  Else
    If iy# < sty2# Then iy# = sty2#
    If iy# > ndy2# Then iy# = ndy2#
  EndIf

  DrawImage cursor, cx#, (iy#*15)+y1
  ;=====

  Delay 60
  
  Flip()
  Wend
EndIf
End Function

Function show_user_params()
  Local backgrnd = LoadImage("Images/params.bmp")
  Local font = LoadFont("Arial", 16, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font
  
  While Not KeyHit(1)
    RenderWorld()

    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)

    x0# = GraphicsWidth()/2 - (ImageWidth(backgrnd)/2) + 10
  y0# = GraphicsHeight()/2 - (ImageHeight(backgrnd)/2) + 10
  Color 255,255,0
  Text x0#, y0#, "Protection: "+user\Armor#
  Text x0#, y0#+20, "Attack: "+user\Damage#
  Text x0#, y0#+40, "Engine speed (MAX): "+user\MaxSpd#  
  Text x0#, y0#+60, "Item picking radius: "+user\PickR#
  Text x0#, y0#+80, "Max item weight: "+user\MaxItems#
  Text x0#, y0#+100, "Money: "+user\Money#
  
  Flip()
  Wend

  SetFont font0
End Function

Function show_quests()
  Local backgrnd = LoadImage("Images/Quests.bmp")
  Local font = LoadFont("Arial", 16, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  Local qc# = 0
  SetFont font
  
    RenderWorld()

    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)
  x0# = GraphicsWidth()/2 - (ImageWidth(backgrnd)/2) + 10
  y0# = GraphicsHeight()/2 - (ImageHeight(backgrnd)/2) + 10
  Color 255,255,0
    
  For i = 1 To QuestCnt
    If (Quest[i]\Active# = 1)
      qc# = qc# + 1
      Text x0#, y0# + (qc#*20), Quest[i]\SValue0$+" (Gived by "+Quest[i]\Giver$+", Target - "+Quest[i]\SValue2$+")"
    EndIf
    
    If (Quest[i]\Done# = 1)
      Color 0, 255, 0
    qc# = qc# + 1
    Text x0#, y0# + (qc#*20), Quest[i]\SValue0$+" (Gived by "+Quest[i]\Giver$+", Target - "+Quest[i]\SValue2$+")"
    EndIf  
  Next  
  
  Flip()

  While Not KeyHit(1)
  Wend

  SetFont font0
End Function

Function message(msg$)
  Local backgrnd = LoadImage("Images/message.bmp")
  MaskImage backgrnd, 0, 0, 0
  Local font = LoadFont("Arial", 16, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  SetFont font
  
  While Not KeyHit(1)
    RenderWorld()

    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)
  Color 255,255,0
  Text GraphicsWidth()/2-(StringWidth(msg$)/2), GraphicsHeight()/2-20, msg$
  s$ = "Press <Esc> to continue"
  Text GraphicsWidth()/2-(StringWidth(s$)/2), GraphicsHeight()/2+20, s$

  Delay 60
  
  Flip()
  Wend

  SetFont font0
  Delay 240
End Function

Function question(msg$, a1$, a2$)
  ;=== IF a1$ THEN TRUE,  IF a2$ THEN FALSE  
  Delay 300
  
  Local backgrnd = LoadImage("Images/message.bmp")
  MaskImage backgrnd, 0, 0, 0
  Local font = LoadFont("Arial", 16, 1)
  Local font0 = LoadFont("Arial", fs, 0)  
  SetFont font
    
    RenderWorld()

    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)
  Color 255,255,0
  Text GraphicsWidth()/2-(StringWidth(msg$)/2), GraphicsHeight()/2-20, msg$
  s$ = a1$+" <Right>; "+a2$+" <Left>"
  Text GraphicsWidth()/2-(StringWidth(s$)/2), GraphicsHeight()/2+20, s$    
  
  Flip()
  
  Repeat
    If KeyHit(203) Then SetFont font0 : Return True
  If KeyHit(205) Then SetFont font0 : Return False

  Delay 60
  Forever

  SetFont font0
  Return False
End Function

Function planet_trade(p.TPlanet)
  Local backgrnd = LoadImage("Images/trade1.bmp")
  Local cursor = LoadImage("Images/tradecursor.bmp")
  Local cx#, cy#, ix#, iy#, sty1#, ndy1#, sty2#, ndy2#, Mode#

  Mode# = 0;Buy

  cx# = GraphicsWidth()/2 - (ImageWidth(backgrnd)/2)
  
  MaskImage backgrnd, 0, 0, 0
  MaskImage cursor, 0, 0, 0
  
  While Not KeyHit(1)
    RenderWorld()

    ;==TRADE==  
  DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)

  y1 = GraphicsHeight()/2 - (ImageHeight(backgrnd)/2) + 10
  y2 = GraphicsHeight()/2 + (ImageHeight(backgrnd)/2) - 10
  For y = y1 To y1+(PlanetItemCnt*15) Step 15
    nn = ((y-y1)/15)+1
      If nn <= PlanetItemCnt
      s$ = "<" + p\name$ + ">  " + PlanetInventory[nn]\SValue0$ + "     " + PlanetInventory[nn]\SValue1$ + "=" 
    s$ = s$ + PlanetInventory[nn]\FValue0# + "    Price:" + PlanetInventory[nn]\FValue1#
    Color 240,215,20
      Text GraphicsWidth()/2 - (ImageWidth(backgrnd)/2) + 10, y, s$
    EndIf      
  Next

    ys# = y1+(PlanetItemCnt*15)+15
  For y = ys# To y2 Step 15
    nn = ((y-ys#)/15)+1
    If nn <= InventoryItemCnt
      s$ = "<Player>   "+Inventory[nn]\SValue0$+"     "+Inventory[nn]\SValue1$+"   "+Inventory[nn]\FValue0#+"   Price:"+Inventory[nn]\FValue1#
    Color 240,215,20
      Text GraphicsWidth()/2 - (ImageWidth(backgrnd)/2) + 10, y, s$    
    EndIf
  Next  

  If KeyDown(200) Then iy# = iy# - 1
  If KeyDown(208) Then iy# = iy# + 1
  If KeyDown(15) Then Mode# = Not Mode#

  sty1# = 0
  ndy1# = PlanetItemCnt-1
  sty2# = ndy1#+2
  ndy2# = sty2#+InventoryItemCnt-1
  
  If KeyHit(28)
    If Mode# = 0
      If (user\money# >= PlanetInventory[iy#+1]\FValue1#) And (question("Buyl "+PlanetInventory[iy#+1]\SValue0$+" for "+PlanetInventory[iy#+1]\FValue1#+" lv?", "Yes", "No") = True)
        user\money# = user\money# - PlanetInventory[iy#+1]\FValue1#
      p\money# = p\money# + PlanetInventory[iy#+1]\FValue1#
      InventoryItemCnt = InventoryItemCnt + 1
      Inventory[InventoryItemCnt] = New TItem
      Inventory[InventoryItemCnt] = PlanetInventory[iy#+1]

      For i = iy#+1 To PlanetItemCnt-1      
      PlanetInventory[i] = PlanetInventory[i+1]
      Next  
      PlanetItemCnt = PlanetItemCnt - 1
      EndIf

    update_user_params()
    Else
      If (p\money# >= Inventory[iy#-PlanetItemCnt]\FValue1#) And (question("Sell "+Inventory[iy#-PlanetItemCnt]\SValue0$+" for "+Inventory[iy#-PlanetItemCnt]\FValue1#+" lv?", "Yes", "No") = True)
        p\money# = p\money# - Inventory[iy#-PlanetItemCnt]\FValue1#
      PlanetItemCnt = PlanetItemCnt + 1
      PlanetInventory[PlanetItemCnt] = New TItem
      PlanetInventory[PlanetItemCnt] = Inventory[iy#+1-PlanetItemCnt]
      user\Money# = user\Money# + Inventory[iy#+1-PlanetItemCnt]\FValue1#
            
      For i = iy#+1-PlanetItemCnt To InventoryItemCnt - 1
        Inventory[i] = Inventory[i+1]
      Next
      InventoryItemCnt = InventoryItemCnt - 1
      EndIf
    EndIf

    update_user_params()
  EndIf

    If Mode# = 0    
    If iy# < sty1# Then iy# = sty1#
    If iy# > ndy1# Then iy# = ndy1#
  Else
    If iy# < sty2# Then iy# = sty2#
    If iy# > ndy2# Then iy# = ndy2#
  EndIf

  DrawImage cursor, cx#, (iy#*15)+y1
  ;=====

  Delay 60
  
  Flip()
  Wend
End Function

Function planet_menu(p.TPlanet)
  message("Welcome to "+p\Name$)
  ;Delay 120
  
  Local backgrnd = LoadImage("Images/trade1.bmp")
  Local cursor = LoadImage("Images/tradecursor.bmp")
  MaskImage cursor, 0, 0, 0
  Local font = LoadFont("Arial", 16, 1)
  Local font0 = LoadFont("Arial", fs, 0)
  Local fl = 0

  For i = 1 To PlanetItemCnt
    PlanetInventory[i] = New TItem
  PlanetInventory[i] = Item[Rnd(ItemCnt-1)+1]
  Next

  iy# = 1  
  
  While Not (KeyHit(1) Or fl = 1)
    SetFont font
    RenderWorld()

    DrawImage backgrnd, GraphicsWidth()/2 - (ImageWidth(backgrnd)/2), GraphicsHeight()/2 - (ImageHeight(backgrnd)/2)
  x0# = GraphicsWidth()/2 - (ImageWidth(backgrnd)/2) + 10
  y0# = GraphicsHeight()/2 - (ImageHeight(backgrnd)/2) + 10
  Color 255,255,0
  
    Text x0#, y0#, "Trade"
    Text x0#, y0#+20, "Repair"
  Text x0#, y0#+40, "Quest"
  Text x0#, y0#+60, "Exit"

  DrawImage cursor, x0#-10, y0#+((iy#-1)*20)

  If KeyDown(208) Then iy# = iy# + 1
  If KeyDown(200) Then iy# = iy# - 1

  If (KeyDown(28))
    If (iy# = 1)
      planet_trade(p)
    EndIf

    If (iy# = 2)
      If (question("Do full ship repair for 1000 lv?", "Yes", "No") = True)
      user\HP# = 100
      user\Armor# = 100
      user\Ammo# = 1000
    EndIf
    EndIf
    
    If (iy# = 3)
      For q.TQuest = Each TQuest
      If (q\Condition# = 2) And (q\Giver$ = p\Name$) And (q\Active# = 1)
        q\Active# = 0
      q\Done# = 1
      message(q\SValue0$+": quest done")
      ElseIf (q\Condition# = 3) And (q\SValue2$ = p\Name$) And (q\Active# = 1)
        q\Active# = 0
      q\Done# = 1
      message(q\SValue0$+": quest done")
      EndIf
    Next

    For q.TQuest = Each TQuest      
      If (p\Name$ = q\Giver$)
        If (q\Active# = 0) And (q\Done# = 0) And (EntityDistance(user\Entity, p\Entity) <= user\PickR#*5) ;We've found it!
        q\Active# = 1
        message("New quest: "+Left(q\SValue0$, 18)+"... (press <Esc> , <Q>)")
          Exit
      EndIf
      EndIf    
    Next
    EndIf

    If (iy# = 4)
      fl = 1
    EndIf
  EndIf

  If iy# < 1 Then iy# = 1
  If iy# > 4 Then iy# = 4

  Delay 60
  
  Flip()
  Wend

  SetFont font0
  Delay 120
End Function

Function pickitem()
  For it.TItem = Each TItem
    If (it\Entity > 0)
    If (KeyDown(56) Or KeyDown(184)) And (EntityDistance(user\Entity, it\Entity) <= user\PickR#)
      FreeEntity it\Entity
    it\Entity = 0
      InventoryItemCnt = InventoryItemCnt+1
      Inventory[InventoryItemCnt] = it
    EndIf  
  EndIf
  Next
End Function

Function create_Shot(owner#, x#,y#,z#,pitch#,yaw#,roll#, damage#)
  Local c
  
  If shots < Max_shots
  s.Tshot=New Tshot
  s\entity=CopyEntity(shot_sprite)
  s\damage# = damage#
  s\owner# = owner#
  shots = shots+1
  PositionEntity s\entity,x#,y#,z#,1
  RotateEntity s\entity,pitch#,yaw#,roll#,1
  Return True
  Else
    c = 0
  
    For a.TShot = Each TShot
    If c >= max_shots / 2
      Exit
      Return True
    EndIf
    
    c = c + 1
    shots = shots - 1
    FreeEntity a\entity
    Delete a        
  Next
  EndIf
End Function

Function update_shot()
  vs#=1
  max_dist#=1000
  For a.Tshot=Each Tshot
    MoveEntity a\entity,0,0,vs#
    a\dist#=a\dist#+vs#
    If (a\dist#>max_dist#)
    FreeEntity a\entity
     Delete a
    shots = shots - 1
    Exit
  Else
  
    ;USER    
    If EntityDistance(user\Entity, a\entity) <= 5 ;EntityCollided(a\entity, USERT) <> 0
      ;user\HP# = user\HP# - a\Damage#
    user\HP# = user\HP# - (a\Damage#/user\Armor#)
      user\Armor# = user\Armor# - (a\Damage#/user\Armor#)
    FreeEntity a\entity
       Delete a

    shots = shots - 1
    Exit
    EndIf
    
    ;BOT
    For t = 1 To BotCnt
      If (EntityDistance(Bot[t]\Entity, a\entity) <= 5) And (a\owner# <> Bot[t]\Entity)
      Bot[t]\HP# = Bot[t]\HP# - (a\Damage#/Bot[t]\Armor#)
      Bot[t]\Armor# = Bot[t]\Armor# - (a\Damage#/10)
      FreeEntity a\entity
         Delete a

      shots = shots - 1
      
      Return 0
    EndIf  
    Next
    
    ;SHOT
    For k.TShot=Each TShot
      If (EntityDistance(user\Entity, a\entity) <= 5) And (k\entity <> a\entity)
        FreeEntity a\entity
      Delete a
      FreeEntity k\entity
      Delete k

      shots = shots - 1
      
      Exit
      EndIf
    Next
  EndIf
  Next
End Function

Function bot_shoot(bn, x#, y#, z#)
  create_shot(Bot[bn]\Entity, EntityX(Bot[bn]\Entity),EntityY(Bot[bn]\Entity),EntityZ(Bot[bn]\Entity),EntityPitch(Bot[bn]\Entity),EntityYaw(Bot[bn]\Entity),EntityRoll(Bot[bn]\Entity),Bot[bn]\Damage#)
End Function

Function bot_shoot2(bn, x#, y#, z#)
  Local v0, v1, v2, k#, xd, yd, zd

  Bot[bn]\AccuracyNow# = Bot[bn]\Accuracy# - (100/Bot[bn]\Speed#)
  
  ;SeedRnd MilliSecs()
  k# = (Rnd(200)/Bot[bn]\AccuracyNow#)
  xd = Rnd(1)
  yd = Rnd(1)
  zd = Rnd(1)

  If xd = 1 Then x# = x# + k# Else x# = x# - k#
  If yd = 1 Then y# = y# + k# Else y# = y# - k#
  If zd = 1 Then z# = z# + k# Else z# = z# - k#

  If Bot[bn]\mesh > 0 Then FreeEntity Bot[bn]\mesh#
  If Bot[bn]\surf > 0 Then ClearSurface Bot[bn]\surf#
  If Bot[bn]\brush > 0 Then FreeBrush Bot[bn]\brush#
  
  Bot[bn]\mesh# = CreateMesh()
  Bot[bn]\surf# = CreateSurface(Bot[bn]\mesh#)
  Bot[bn]\brush# = CreateBrush(255, 50, 0)
  
  PaintSurface Bot[bn]\surf#, Bot[bn]\Brush#
  EntityAlpha Bot[bn]\mesh#, 0.1
  
  v0 = AddVertex(Bot[bn]\surf#, Bot[bn]\x#-5, Bot[bn]\y#, Bot[bn]\z#)
  v1 = AddVertex(Bot[bn]\surf#, x#, y#, z#)
  v2 = AddVertex(Bot[bn]\surf#, Bot[bn]\x#+5, Bot[bn]\y#, Bot[bn]\z#)
  AddTriangle(Bot[bn]\surf#, v0, v1, v2)

  v0 = AddVertex(Bot[bn]\surf#, x#-0.5, y#, z#)
  v1 = AddVertex(Bot[bn]\surf#, Bot[bn]\x#, Bot[bn]\y#, Bot[bn]\z#)
  v2 = AddVertex(Bot[bn]\surf#, x#+0.5, y#, z#)
  AddTriangle(Bot[bn]\surf#, v0, v1, v2)

  p = CreatePivot()
  PositionEntity p, x#, y#, z#
  If (EntityDistance(user\Entity, p) <= user\ProtectionR#)
    user\HP# = user\HP# - (Bot[bn]\Damage#/user\Armor#)
  user\Armor# = user\Armor# - (Bot[bn]\Damage#/user\Armor#)
  EndIf  
End Function

Function bot_death(x#, y#, z#)
  booms = booms + 1
  boom[booms] = CreateEntity()
  boomsizes[i] = 5
  EntityTexture boom[booms], boomtex
  EntityAlpha boom[booms], 0.25
  PositionEntity boom[booms], x#, y#, z#  
End Function

Function update_booms()
  For i = 1 To booms
    If boomsizes[i] < 50
    boomsizes[i] = boomsizes[i] + 1
    ScaleEntity boom[i], boomsizes[i], boomsizes[i], boomsizes[i]
  Else    
    FreeEntity boom[i]    
    
    For t = i To booms - 1
      boom[t] = boom[t+1]
    boomsizes[t] = boomsizes[t+1]
    Next

    boomsizes[i] = 0    
    booms = booms - 1
  EndIf  
  Next
End Function

Function update_ai1(i)
;ALIEN INVADER
    If (Bot[i]\Race# >= 60) Or (Bot[i]\Character# < 25)
    If (EntityDistance(Bot[i]\Entity, user\Entity) <= Bot[i]\ViewR#) And (EntityDistance(Bot[i]\Entity, user\Entity) > Bot[i]\RunR#)  And (user\HP# > 0)
      Bot[i]\Xt# = user\X#
      Bot[i]\Yt# = user\Y#
        Bot[i]\Zt# = user\Z#
      Bot[i]\Speed# = 0.25

      If pvt = 0 Then pvt = CreatePivot()
      PositionEntity pvt, Bot[i]\Xt#, Bot[i]\Yt#, Bot[i]\Zt#
      PointEntity Bot[i]\Entity, pvt
      MoveEntity Bot[i]\Entity, 0, 0, Bot[i]\Speed#

      Bot[i]\X# = EntityX(Bot[i]\Entity)
      Bot[i]\Y# = EntityY(Bot[i]\Entity)
      Bot[i]\Z# = EntityZ(Bot[i]\Entity)
    ElseIf (EntityDistance(Bot[i]\Entity, user\Entity) <= Bot[i]\RunR#)
      SeedRnd MilliSecs()
      z = Rnd(1)
    y = Rnd(1)
    x = Rnd(1)
    d = Rnd(1)

    If d = 0 Then k = Bot[i]\AttackR# Else
    If d = 1 Then k = -Bot[i]\AttackR# Else

    If z = 1 Then Bot[i]\Xt# = user\X#+k Else
    If y = 1 Then Bot[i]\Yt# = user\Y#+k Else
        If x = 1 Then Bot[i]\Zt# = user\Z#+k
      Bot[i]\Speed# = 0.5

      If pvt = 0 Then pvt = CreatePivot()
      PositionEntity pvt, Bot[i]\Xt#, Bot[i]\Yt#, Bot[i]\Zt#
      PointEntity Bot[i]\Entity, pvt
      MoveEntity Bot[i]\Entity, 0, 0, Bot[i]\Speed#

      Bot[i]\X# = EntityX(Bot[i]\Entity)
      Bot[i]\Y# = EntityY(Bot[i]\Entity)
      Bot[i]\Z# = EntityZ(Bot[i]\Entity)
    EndIf

    If (EntityDistance(user\Entity, Bot[i]\Entity) <= Bot[i]\AttackR#) Then bot_shoot(i, user\X#, user\Y#, user\Z#)    
  EndIf
End Function

Function update_ai2(i)
;KILLER
  If (Bot[i]\BotType# < 25)
    If ((Bot[i]\Target = 0) Or (Bot[i]\TargetOk# = 0)) ;if we are killer and we haven't a target
      ;Searching for a target
      For b.TBot = Each TBot
        If (b\entity <> Bot[i]\Entity) And (b\BotType > 25) And (EntityDistance(Bot[i]\Entity, b\entity) <= Bot[i]\ViewR#) And (b\Dead# = 0)
        Bot[i]\Xt# = b\X#
        Bot[i]\Yt# = b\Y#
        Bot[i]\Zt# = b\Z#
        Bot[i]\Speed# = 1
            Bot[i]\Target = b\Entity
        Bot[i]\TargetOK# = 1
        Exit
      EndIf
      
      ;If target is dead
        If (b\HP# <= 0) Or (b\Dead# = 1) Or (Bot[i]\TargetOk# = 0)
             Bot[i]\Target = 0
          Bot[i]\TargetOk# = 0          
          EndIf
      Next  
        
    ;EndIf  
    ElseIf (Bot[i]\Target <> 0) ;if we already have a target
      If (EntityDistance(Bot[i]\Entity, Bot[i]\Target) <= Bot[i]\RunR#) ;Following back  ;And (EntityDistance(Bot[i]\Entity, Bot[i]\Target)) ;if target is rather far and rather near
        SeedRnd MilliSecs()
        z = Rnd(1)
      y = Rnd(1)
      x = Rnd(1)
      d = Rnd(1)

      If d = 0 Then k = Bot[i]\AttackR# Else
      If d = 1 Then k = -Bot[i]\AttackR# Else

      If z = 1 Then Bot[i]\Xt# = Bot[i]\Xt#+k Else
      If y = 1 Then Bot[i]\Yt# = Bot[i]\Yt#+k Else
          If x = 1 Then Bot[i]\Zt# = Bot[i]\Zt#+k
        Bot[i]\Speed# = 0.5

      If pvt = 0 Then pvt=CreatePivot()
      PositionEntity pvt, Bot[i]\Xt#, Bot[i]\Yt#, Bot[i]\Zt#
        PointEntity Bot[i]\Entity, pvt
        MoveEntity Bot[i]\Entity, 0, 0, Bot[i]\Speed#        
    EndIf    

    ;Pointing to a target    
      PointEntity Bot[i]\Entity, Bot[i]\Target
    
    If (Bot[i]\TargetOk# = 1) And (EntityDistance(Bot[i]\Entity, Bot[i]\Target) <= Bot[i]\ViewR#) And (EntityDistance(Bot[i]\Entity, Bot[i]\Target) > Bot[i]\RunR#)
      ;Pointing to a target    
        PointEntity Bot[i]\Entity, Bot[i]\Target
    MoveEntity Bot[i]\Entity, 0, 0, Bot[i]\Speed#
      EndIf    

    If (EntityDistance(Bot[i]\Target, Bot[i]\Entity) <= Bot[i]\AttackR#) Then bot_shoot(i, Bot[i]\Xt#, Bot[i]\Yt#, Bot[i]\Zt#)    
  EndIf
  EndIf

    For t.TBot = Each TBot
    If (t\Entity = Bot[i]\Target)
      If (t\Dead# = 1) Or (t\HP# <= 0)
      Bot[i]\Target = 0
      Bot[i]\TargetOk# = 0
    EndIf  
    EndIf  
  Next
;  EndIf

  Bot[i]\X# = EntityX(Bot[i]\Entity)
  Bot[i]\Y# = EntityY(Bot[i]\Entity)
  Bot[i]\Z# = EntityZ(Bot[i]\Entity)    
End Function

Function update_ai3(i)
  If Bot[i]\Entity = 0 Then Return
  
  ;FIGHTER
  If (Bot[i]\BotType# >= 50) And (Bot[i]\BotType# < 75) ;if we are fighter
    If (Bot[i]\Target = 0) Or (Bot[i]\TargetOk# = 0) ;and we haven't target
      ;search for it!
      For b.TBot = Each TBot
        If (b\entity <> Bot[i]\Entity)
        If (b\HP# > 0) And ((b\BotType < 25) Or (b\Race# >= 60) Or (b\BotType <= 25))
            Bot[i]\Xt# = b\X#
          Bot[i]\Yt# = b\Y#
          Bot[i]\Zt# = b\Z#
          Bot[i]\Speed# = 3
              Bot[i]\Target = b\Entity
          Bot[i]\TargetOK# = 1
          Exit
      EndIf  
      EndIf    
      Next  

        ;looking for a live target
      For fe.TBot = Each TBot        
      If (fe\Entity = Bot[i]\Entity) And (fe\HP# <= 0)
            Bot[i]\Target = 0
        Bot[i]\TargetOk# = 0
        Exit
        EndIf
      Next              

    ElseIf (Bot[i]\Target <> 0) ;if we have a target
        If (EntityDistance(Bot[i]\Entity, Bot[i]\Target) <= Bot[i]\RunR#) ;following back
          SeedRnd MilliSecs()
          z = Rnd(1)
        y = Rnd(1)
        x = Rnd(1)
        d = Rnd(1)

        If d = 0 Then k = Bot[i]\AttackR# Else
        If d = 1 Then k = -Bot[i]\AttackR# Else

        If z = 1 Then Bot[i]\Xt# = Bot[i]\Xt#+k Else
        If y = 1 Then Bot[i]\Yt# = Bot[i]\Yt#+k Else
            If x = 1 Then Bot[i]\Zt# = Bot[i]\Zt#+k
          Bot[i]\Speed# = 0.5

        If pvt1 = 0 Then pvt1 = CreatePivot()
        PositionEntity pvt1, Bot[i]\Xt#, Bot[i]\Yt#, Bot[i]\Zt#
          PointEntity Bot[i]\Entity, pvt1
          MoveEntity Bot[i]\Entity, 0, 0, Bot[i]\Speed#

          Bot[i]\X# = EntityX(Bot[i]\Entity)
          Bot[i]\Y# = EntityY(Bot[i]\Entity)
          Bot[i]\Z# = EntityZ(Bot[i]\Entity)
          EndIf              

      If (EntityDistance(Bot[i]\Target, Bot[i]\Entity) <= Bot[i]\ViewR#)
        PointEntity Bot[i]\Entity, Bot[i]\Target
      EndIf

        If (EntityDistance(Bot[i]\Target, Bot[i]\Entity) <= Bot[i]\AttackR#) ;shooting at target
          bot_shoot(i, Bot[i]\Xt#, Bot[i]\Yt#, Bot[i]\Zt#)
        EndIf    

        ;Moving to a target
        If (EntityDistance(Bot[i]\Entity, Bot[i]\Target) <= Bot[i]\ViewR#) And (EntityDistance(Bot[i]\Entity, Bot[i]\Target) > Bot[i]\RunR#)
        MoveEntity Bot[i]\Entity, 0, 0, Bot[i]\Speed#
        EndIf
      EndIf
    EndIf

    For fe.TBot = Each TBot
    If (Bot[i]\Target <> 0) And (fe\Entity = Bot[i]\Target)
      If (fe\HP# <= 0) Or (fe\Dead# = 1)
        Bot[i]\Target = 0
        Bot[i]\TargetOk# = 0
        Exit
      EndIf  
    EndIf
    Next    

  Bot[i]\X# = EntityX(Bot[i]\Entity)
  Bot[i]\Y# = EntityY(Bot[i]\Entity)
  Bot[i]\Z# = EntityZ(Bot[i]\Entity)
End Function

Function update_world()
  ;Update planets
  For i = 1 To PlanetsCnt  
    PositionEntity Planet[i]\Entity, Planet[i]\X#+Sin(Planet[i]\Xa#)*Planet[i]\Width#, Planet[i]\Y#+Cos(Planet[i]\Ya#)*Planet[i]\Height#, Planet[i]\Z#+Sin(Planet[i]\Za#)*Planet[i]\Depth#
  Planet[i]\Xa# = Planet[i]\Xa#+0.000001
  Planet[i]\Ya# = Planet[i]\Ya#+0.000001
  Planet[i]\Za# = Planet[i]\Za#+0.000001
  RotateEntity Planet[i]\Entity, Planet[i]\Xa#, Planet[i]\Ya#, Planet[i]\Za#  
  Next

  ;Update bots
  For i = 1 To BotCnt
    If (Bot[i]\HP# <= 0) And (Bot[i]\Dead# = 0)
      bot_death(Bot[i]\X#, Bot[i]\Y#, Bot[i]\Z#)
    EntityAlpha Bot[i]\Sphere, 0.001

    For j = 1 To Bot[i]\ItemsCnt#
      BotItems(i, j)\Entity = LoadMesh("Meshes/Pod.x")
    tex = LoadTexture("Meshes/tex27.bmp")
    ScaleEntity BotItems(i, j)\Entity, 0.1, 0.1, 0.1
    EntityTexture BotItems(i, j)\Entity, tex
    FreeTexture tex

    If (BotItems(i, j)\SValue1$ = "Damage") Then EntityColor BotItems(i, j)\Entity, 128,0,0 Else
    If (BotItems(i, j)\SValue1$ = "Protection") Then EntityColor BotItems(i, j)\Entity, 64,0,128 Else
    If (BotItems(i, j)\SValue1$ = "Count") Then EntityColor BotItems(i, j)\Entity, 64,128,128 Else
    If (BotItems(i, j)\SValue1$ = "Speed") Then EntityColor BotItems(i, j)\Entity, 0,64,128 Else
    If (BotItems(i, j)\SValue1$ = "View") Then EntityColor BotItems(i, j)\Entity, 255,255,128        
    
    PositionEntity BotItems(i, j)\Entity, Bot[i]\x#, Bot[i]\y#, Bot[i]\z#
        
    RotateEntity BotItems(i, j)\Entity, Rnd(180)+MilliSecs(), Rnd(90)*2, Rnd(360)
    MoveEntity botitems(i, j)\Entity, Rnd(100)/17, Rnd(10)*2, Rnd(10)+1    
    Next
        
    EntityAlpha Bot[i]\Entity, 0.1    
    Bot[i]\Dead# = 1
  ElseIf (Bot[i]\HP#>0) And (Bot[i]\Dead# = 0)
    update_ai1(i)
    update_ai2(i)
    update_ai3(i)
  EndIf

  PositionEntity Bot[i]\Sphere, EntityX(Bot[i]\Entity), EntityY(Bot[i]\Entity), EntityZ(Bot[i]\Entity)
  Next

  update_booms()
  update_shot()
End Function

;==================PROGRAM START=================
main_menu()

If snd_enable = 1 Then PlaySound(snd1)

Repeat
  update_world()
  update_user()  
Forever

End
