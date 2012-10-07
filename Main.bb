f = ReadFile("Config.ini")
  w=Int(ReadLine(f))
  h=Int(ReadLine(f))
  d=Int(ReadLine(f))
CloseFile(f)

Graphics3D w, h, d
SetBuffer BackBuffer()

;===========TYPES, GLOBALS AND CONSTANTS==============
Type TPlayer
  Field Name$
  Field Entity#
  ;Position
  Field X#
  Field Y#
  Field Z#
  ;Bent (наклон)
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
End Type

Type TPlanet
  Field Name$
  Field Entity#
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
  Field Entity#
  ;Position
  Field X#
  Field Y#
  Field Z#
  ;Angles
  Field Xa#
  Field Ya#
  Field Za#
  ;Target coords
  Field Xt#
  Field Yt#
  Field Zt#
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
  Field Character# ;0..25 - Enemy, 25..75 - Neutral, 75..100 - Friend
  Field BotType# ;0..25 - Killer, 25..50 - Trader, 50..75 - Fighter, 75..100 - Scinetist
  Field Race# ;0..30 - Human, 30..60 - Alien, 60..100 - Invader
End Type

Global user.TPlayer, camera, light, compass
Global PlanetsCnt, ItemCnt, BotCnt
Global Planet.TPlanet[100], Item.TItem[100], Bot.TBot[100]
Dim BotItems.TItem(1000, 100) ;(Bot, ItemNum)
  
Function create_world()
  user = New TPlayer
  user\Entity = CreateSphere()
  user\x# = 0
  user\y# = 0
  user\z# = 0
  user\xa# = 0
  user\ya# = 0
  user\za# = 0
  user\MaxSpd# = 25
  PositionEntity user\Entity#, user\x#, user\y#, user\z#
  RotateEntity user\Entity#, user\xa#, user\ya#, user\za#
  EntityColor user\Entity#, 5, 5, 145  

  camera = CreateCamera()
  PositionEntity camera, user\x#, user\y#+2.5, user\z#-5
  ;RotateEntity camera, 65, 0, 0
  EntityParent camera, user\Entity#
  CameraRange camera, 0.1, 1000

  light = CreateLight()
  LightColor light, 255, 255, 255
  PositionEntity light, 0, 25, 0

  plane = CreatePlane()
  EntityColor plane, 100, 100, 0
  EntityShininess plane, 100
  EntityAlpha plane, 0.5

  f = ReadFile("World.map")
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

	Planet[i]\Entity# = CreateSphere()
	ScaleEntity Planet[i]\Entity#, Planet[i]\PScale#, Planet[i]\PScale#, Planet[i]\PScale#
	PositionEntity Planet[i]\Entity#, Planet[i]\X#, Planet[i]\Y#, Planet[i]\Z#
  Next
	
  CloseFile(f)

  f = ReadFile("Bots.map")
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
      Bot[i]\InventoryNum# = Float(ReadLine(f))

	  Bot[i]\Character# = Float(ReadLine(f))
	  Bot[i]\BotType# = Float(ReadLine(f))
	  Bot[i]\Race# = Float(ReadLine(f))

	  Bot[i]\Entity = CreateSphere()
	  RotateEntity Bot[i]\Entity, Bot[i]\Xa#, Bot[i]\Ya#, Bot[i]\Za#
	  PositionEntity Bot[i]\Entity, Bot[i]\X#, Bot[i]\Y#, Bot[i]\Z#

	  If Bot[i]\Character# < 25 Then EntityColor Bot[i]\Entity, 255, 0, 0 Else
	  If (Bot[i]\Character# >= 25) And (Bot[i]\Character# <= 75) Then EntityColor Bot[i]\Entity, 255,255,0 Else
	  If Bot[i]\Character# > 75 Then EntityColor Bot[i]\Entity, 0, 255, 0 Else
	Next
  CloseFile(f)
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
  Color 255, 255, 255
    
  For i = st To nd Step 2
    a = (stx - 2)+Rnd(xstep+2)
	Line oa, oi, a, i
	oa = a
	oi = i
  Next

  Color 20, 115, 105  
  Line ndx, ndy, ndx, ndy+10
  ;===END INDICATOR===
End Function

Function BotDetector()
  ;====BOT DETECTOR====
  font = LoadFont("Arial", 12)
  SetFont font
  For i = 1 To BotCnt
    CameraProject(camera,EntityX(Bot[i]\Entity),EntityY(Bot[i]\Entity),EntityZ(Bot[i]\Entity))
	If EntityInView(Bot[i]\Entity#, camera)
	  If Bot[i]\BotType# < 25 Then Color 255, 150, 75 Else
	  If (Bot[i]\BotType# >= 25) And (Bot[i]\BotType# < 50) Then Color 255, 255, 75 Else
	  If (Bot[i]\BotType# >= 50) And (Bot[i]\BotType# < 75) Then Color 255, 255, 175 Else
	  If (Bot[i]\BotType# >= 75) Then Color 0, 255, 0
	  
	  Text ProjectedX#()-StringWidth(Bot[i]\Name$)/2, ProjectedY#(), Bot[i]\Name$
	EndIf  
  Next	
  ;====END DETECTOR====
End Function

Function update_user()
  If KeyDown(200) Then user\Xa# = user\Xa#-1;TurnEntity user\Entity#, 1, 0, 0
  If KeyDown(208) Then user\Xa# = user\Xa#+1 ;TurnEntity user\Entity#, -1, 0, 0
  If KeyDown(203) Then user\Za# = user\Za#+1;TurnEntity user\Entity#, 0, 1, 0
  If KeyDown(205) Then user\Za# = user\Za#-1 ;TurnEntity user\Entity#, 0, -1, 0

  RotateEntity user\Entity, user\Xa#, user\Ya#, user\Za#
  
  If KeyDown(157) 
    If user\Speed# < user\MaxSpd# Then user\Speed# = user\Speed# + 1
  EndIf	  

  If KeyDown(54)
    If user\Speed# > 0 Then user\Speed# = user\Speed# - 1
  EndIf	  

  MoveEntity user\Entity#, 0, 0, user\Speed#/10

  UpdateWorld()
  RenderWorld()

  ;=============2D OPERATIONS=============  

  ShockIndicator(10, 10, 10, 100, 5, 10)
  BotDetector()

  ;===ROTATION INDICATOR===
  ;Angle to display, coords of center
  Local STAngle#, X#, Y#
  
  ;===END INDICATOR===

  ;==============END 2D================

  Flip()
End Function

Function update_world()  
  For i = 1 To PlanetsCnt  
    PositionEntity Planet[i]\Entity#, Planet[i]\X#+Sin(Planet[i]\Xa#)*Planet[i]\Width#, Planet[i]\Y#+Cos(Planet[i]\Ya#)*Planet[i]\Height#, Planet[i]\Z#+Sin(Planet[i]\Za#)*Planet[i]\Depth#
	Planet[i]\Xa# = Planet[i]\Xa#+0.000001
	Planet[i]\Ya# = Planet[i]\Ya#+0.000001
	Planet[i]\Za# = Planet[i]\Za#+0.000001
	RotateEntity Planet[i]\Entity#, Planet[i]\Xa#, Planet[i]\Ya#, Planet[i]\Za#	
  Next

  For i = 1 To BotCnt
  
    If Bot[i]\BotType# < 25
	  ;KILLER	  
	  n = 1
	  
	  For t = 1 To BotCnt
	    If (Bot[t]\BotType# >=25) And (EntityDistance(Bot[i]\Entity#, Bot[t]\Entity#) <= Bot[i]\ViewR#) Then
		  n = t
		  Exit
		EndIf  
	  Next

	  Bot[i]\Xt# = Bot[n]\X#
	  Bot[i]\Yt# = Bot[n]\Y#
	  Bot[i]\Zt# = Bot[n]\Z#	  
	Else
	 ;NOT A KILLER
	  If Bot[i]\BotType# >= 25
	    n = 1
		fl = 0
	  
	    For t = 1 To BotCnt
	      If (Bot[t]\BotType# < 25) And (EntityDistance(Bot[i]\Entity#, Bot[n]\Entity#) <= Bot[i]\ViewR#) Then
  		    n = t
			fl = 1
		    Exit
		  EndIf  
	    Next

		If fl = 0 Then Bot[i]\Zt# = Bot[i]\Z# + 50

	    Bot[i]\Xt# = Bot[n]\X#
	    Bot[i]\Yt# = Bot[n]\Y#
	    Bot[i]\Zt# = Bot[n]\Z#	  
	  EndIf
	EndIf  

    If pvt = 0 Then pvt = CreatePivot()
	PositionEntity pvt, Bot[i]\Xt#, Bot[i]\Yt#, Bot[i]\Zt#
	PointEntity Bot[i]\Entity#, pvt
	MoveEntity Bot[i]\Entity#, 0, 0, 2

	Bot[i]\X# = EntityX(Bot[i]\Entity#)
	Bot[i]\Y# = EntityY(Bot[i]\Entity#)
	Bot[i]\Z# = EntityZ(Bot[i]\Entity#)
  Next
End Function

;==================PROGRAM START=================
create_world()

While Not KeyHit(1)
  update_world()
  update_user()  
Wend

End