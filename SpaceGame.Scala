package cs2.game

import cs2.util.Vec2

import java.net.URL
import javax.sound.sampled._
import sun.audio.{ AudioPlayer, AudioStream }
import scalafx.Includes.eventClosureWrapperWithParam
import scalafx.Includes.jfxKeyEvent2sfx
import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp
import scalafx.event._
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Button
import scalafx.scene.image.Image
import scalafx.scene.input.KeyEvent
import scala.collection._
import scalafx.event.EventHandler
import scalafx.scene.input._
import scalafx.event._
import scalafx.scene.control.Label
import scalafx.scene.layout.GridPane
import scalafx.scene.input.KeyCode
import scalafx.scene.layout.BackgroundImage
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color
import java.io.ByteArrayInputStream
import scala.collection.mutable.ArrayStack
import java.io.FileInputStream
import scalafx.scene.media.MediaPlayer
import scalafx.scene.media.Media
import scala.reflect.io.File
import java.io._


/**
 * main object that initiates the execution of the game, including construction
 *  of the window.
 *  Will create the stage, scene, and canvas to draw upon. Will likely contain or
 *  refer to an AnimationTimer to control the flow of the game.
 */
object SpaceGameApp extends JFXApp {

  stage = new JFXApp.PrimaryStage {
    title = "Beth and Jerry!"
    scene = new Scene(400, 600) {
      var gameStates = mutable.Stack[GameState]()
      val canvas = new Canvas(400, 600)
      val g = canvas.graphicsContext2D

      var Startscreen = true
      var Endscreen = false
      var Stack = new ArrayStack[GameState]
      var twoPlayer = false

      // Rewind Function
      var rewind = false
      var canRewind = false
      var allowedtoRewind = false
      var rewindTimer = 59.0

      // User Interfaces
      var background = new Background(new Image("file:Gip.gif"))
      val explodes = new Explosion(new Image("file:Explosion.png"), new Vec2(0, 0), new Vec2(0, 0))
      val background2 = new Background(new Image("file:59439_rick_and_morty_giant_heads-02.png"))
      var background3 = new Background(new Image("file:fog.gif"))
      var background4 = new Background(new Image("file:Loser.gif"))

      // Player Functions
      val newAvatar = new Image("file:Plimbus.gif")
      val newInitPos = new Vec2(200, 525)
      val newBulletpic = new Image("file:Spaceship.png")
      val playersize = new Vec2(50, 50)
      val player = new Player(newAvatar, new Vec2(100, 525), newBulletpic, playersize)
      val player2 = new Player(new Image("file:Spaceship.png"), newInitPos, newBulletpic, playersize)
      var Lazer = false
      var PlayerLives = 1000
      var PlayerScore = 0

      // Enemy Functions
      val enemy = new Enemy(new Image("file:EnemySwarm.png"), new Vec2(0, 0), new Image("file:Bullet.png"), new Vec2(50, 50))
      var newEnemySwarm = new EnemySwarm(7, 3, new Vec2(math.random - 1.0, math.random - 1.0), new Image("file:EnemySwarm.png"))

      // Boss
      val Boss = new Boss(new Image("file:Head.png"), new Vec2(100, 200), new Image("file:EnemySwarm.png"), new Vec2(200, 200))
      val Boss2 = new Boss(new Image("file:Explosion.png"), new Vec2(100, 200), new Image("file:EnemySwarm.png"), new Vec2(200, 200))
      var BossHp = 300

      // Sets
      var playerSet = Set[Sprite]()
      var moveMent = Set[String]()
      var Bullet = Set[Bullet]()

      //Buffer
      var bullets = mutable.Buffer[Bullet]() //(Player Bullets)
      var enbullets = mutable.Buffer[Bullet]() //(Enemy Bullets)
      var BossBullets = mutable.Buffer[Bullet]() // (Boss's Bullet)
      var DeadParts = mutable.Buffer[Sprite]()
      var EnemySwarm = mutable.Buffer[Enemy]()
      var swarmBuffer = mutable.Buffer[EnemySwarm]()

      var deadSwarmBuffer = mutable.Buffer[EnemySwarm]()
      var DeadBoss = mutable.Buffer[Boss]()

      // Functions
      def ResetSwarm() = {
        if (EnemySwarm.length == 21) {
          EnemySwarm.clear()
          newEnemySwarm.reset()
        }
      }

      def BossDisplay(): Boolean = {
        if (EnemySwarm.length >= math.random * 100) {
          true
        } else {
          false
        }
      }

      def play(bytes: Array[Byte]) {
        val audioStream = new AudioStream(new ByteArrayInputStream(bytes))
        AudioPlayer.player.start(audioStream)
      }
      def reverse() = {
        var newBullets = mutable.Buffer[Bullet]()
        for (ps <- bullets) {
          bullets += new Bullet(ps.pic, new Vec2(ps.initPos.x, ps.initPos.y), ps.vel, ps.size)
        }
        var newEnemy = new EnemySwarm(7, 3, new Vec2(math.random - 1.0, math.random - 1.0), newEnemySwarm.swarm(0).pic)
        var newEnemySwarm2 = mutable.Buffer[Enemy]()
        for (Es <- newEnemySwarm.swarm) {
          newEnemySwarm2 += new Enemy(Es.pic, new Vec2(Es.initPos.x, Es.initPos.y), Es.bulletPic, Es.size)

        }

        var newEnbullets = mutable.Buffer[Bullet]()
        for (ps <- enbullets) {
          enbullets += new Bullet(ps.pic, new Vec2(ps.initPos.x, ps.initPos.y), ps.vel, ps.size)
        }

        newEnemy.swarm = newEnemySwarm2.reverse
        var GameState = new GameState(
          new Player(player.avatar, new Vec2(player.pos.x, player.pos.y), player.bulletPic, player.size), newEnemy,
          newEnbullets, newBullets,
          PlayerLives, PlayerScore)

        Stack.push(GameState)
      }
      //Animation Time

      var prevTime: Long = 0
      var priorTime: Long = 0
      var timeLaspe = 0
      var timers = 500
      var s = 0
      var b = 5
      var swarmTimer = 0
      var lastTime = 0L
      val timer = AnimationTimer(t => {
        if (t - priorTime > 10) {
          val delta = (t - lastTime) / 1e9

          if (Startscreen == false && Endscreen == false) {

            if (twoPlayer == true) {
              val score2 = new Label("Player 2 Score" + " " + PlayerScore.toString())
              score2.layoutX_=(0)
              score2.layoutY_=(30)
              score2.setTextFill(Color.WHITE)

              val Lives = new Label("Player Lives" + " " + PlayerLives.toString())
              Lives.setTextFill(Color.WHITE)

              val score = new Label("Player Score" + " " + PlayerScore.toString())
              score.layoutX_=(250)
              score.layoutY_=(0)
              score.setTextFill(Color.WHITE)

              val gridPane = new GridPane

              val rewindLabel = new Label("Rewind:")
              gridPane.add(rewindLabel, 2, 0)
              rewindLabel.layoutX = 250
              rewindLabel.layoutY = 15
              rewindLabel.setTextFill(Color.WHITE)

              val rewindRect = new Rectangle
              rewindRect.height = 20
              rewindRect.width = 64
              rewindRect.fill = Color.WHITE
              rewindRect.layoutX = 300
              rewindRect.layoutY = 15
              rewindRect.stroke

              gridPane.add(rewindRect, 2, 1)

              val rewindRect2 = new Rectangle
              var r = 0
              rewindRect2.height = 16
              rewindRect2.width = r //maxis60

              rewindRect2.layoutX = 300
              rewindRect2.layoutY = 15
              rewindRect2.fill = Color.Green

              gridPane.layoutX = 200
              gridPane.layoutY = -525

              content = List(canvas, score, score2, Lives, gridPane, rewindLabel, rewindRect, rewindRect2)

            } else if (twoPlayer == true || twoPlayer == false) {

              //g.clearRect(0, 0, 400, 600)
              val Lives = new Label("Player Lives" + " " + PlayerLives.toString())
              Lives.setTextFill(Color.WHITE)

              val score = new Label("Player Score" + " " + PlayerScore.toString())
              score.layoutX_=(250)
              score.layoutY_=(0)
              score.setTextFill(Color.WHITE)

              val gridPane = new GridPane

              val rewindLabel = new Label("Rewind:")
              gridPane.add(rewindLabel, 2, 0)
              rewindLabel.layoutX = 250
              rewindLabel.layoutY = 15
              rewindLabel.setTextFill(Color.WHITE)

              val rewindRect = new Rectangle
              rewindRect.height = 20
              rewindRect.width = 64
              rewindRect.fill = Color.WHITE
              rewindRect.layoutX = 300
              rewindRect.layoutY = 15
              rewindRect.stroke

              gridPane.add(rewindRect, 2, 1)

              val rewindRect2 = new Rectangle
              var r = 0
              rewindRect2.height = 16
              rewindRect2.width = r //maxis60

              rewindRect2.layoutX = 300
              rewindRect2.layoutY = 15
              rewindRect2.fill = Color.Green

              gridPane.layoutX = 200
              gridPane.layoutY = -525

              content = List(canvas, score, Lives, gridPane, rewindLabel, rewindRect, rewindRect2)
            }
          }
          // reverse

          // Player Display and Bullet Display
          background.display(g)

          player.display(g)

          if (twoPlayer == true) {
            player2.display2(g)
          }
          enbullets.foreach { x => x.timeStep() }
          enbullets.foreach { x => x.display(g) }

          newEnemySwarm.display(g)
          bullets.foreach { x => x.timeStep() }
          bullets.foreach { x => x.display(g) }

          var BulletTimer = 0
          if (BulletTimer % 4 == 1 || BulletTimer == 0) {
            BulletTimer += 1
            // enbullets += newEnemySwarm.shoot()
            // enbullets.remove(4)

          }
///////******Might Need to Comment out *****//////
          //reverse()

          // Key Movement/Player Movement

          canvas.onKeyPressed = (e: KeyEvent) => {
            if (e.code.toString == "X") {
              println("X")
              twoPlayer = true
            } else {
              if (e.code.toString == "ESCAPE") {
                System.exit(0)
              } else {
                if (e.code.toString == "R" && !Stack.isEmpty) {
                  val newGameState = Stack.pop()
                  println("R Key was Pressed")
                } else {
                  if (swarmTimer % 3 == 0 && e.code.toString == "SPACE") {
                    bullets += player.shoot()
                    moveMent += e.code.toString()
                  } else {
                    if (e.code.toString == "LEFT" && twoPlayer == true || e.code.toString == "LEFT" && twoPlayer == false) {
                      player.moveLeft
                      if (swarmTimer % 3 == 0) {
                        Boss.moveLeft()
                      }
                      println("Left Key was Pressed")
                      moveMent += e.code.toString()
                    } else {
                      if (e.code.toString == "RIGHT") {
                        player.moveRight

                        if (swarmTimer % 3 == 0) {
                          Boss.moveRight()
                        }
                        println("Right Key was Pressed")
                        moveMent += e.code.toString()
                      } else {

                        //Bullet Deployment
                        if (e.code.toString == "UP" && player.isOffscreen == false) {
                          player.moveUp()
                          if (swarmTimer % 3 == 0) {
                            Boss.moveDown()
                          }
                          println("Up Key was Pressed")
                          moveMent += e.code.toString()
                        } else {
                          if (e.code.toString == "DOWN" && player.isOffscreen == false) {
                            player.moveDown()
                            Boss.moveUp()
                            println("Down Key was Pressed")
                            moveMent += e.code.toString()
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          if (twoPlayer == true) {

            canvas.onKeyReleased = (e: KeyEvent) => {

              if (e.code.toString == "ENTER") {
               Lazer = true
              } else {
              if (e.code.toString == "X") {
                println("X")
                twoPlayer = true
              } else {
                if (e.code.toString == "ESCAPE") {
                  System.exit(0)
                } else {
                  if (swarmTimer % 3 == 0 && e.code.toString == "Z") {
                    bullets += player2.shoot()
                  } else {
                    if (e.code.toString == "A") {
                      player2.moveLeft
                    } else {
                      if (e.code.toString == "D") {
                        player2.moveRight
                      } else {

                        //Bullet Deployment
                        if (e.code.toString == "W" && player.isOffscreen == false) {
                          player2.moveUp()

                        } else {
                          if (e.code.toString == "S" && player.isOffscreen == false) {
                            player2.moveDown()

                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            }
            }

          // Swarm Movement
          var n = 0
          if (swarmTimer % 2 == 1 || swarmTimer == 0) {
            swarmTimer += 1

          } else {
            if (s >= 0 && s <= 2 && b <= 5 && b >= 3) {

              swarmTimer += 1
              newEnemySwarm.movedown()

              b -= 1
              s += 1

              Boss.move(new Vec2(n + 1, 0))
            } else {
              if (s >= 2 && s <= 4) {
                Boss.move(new Vec2(n - 1, 0))
                newEnemySwarm.moveup()

                s += 1

              } else {
                if (s >= 4 && s <= 6) {
                  newEnemySwarm.moveStop()
                  // Boss.move(new Vec2(20, 0))
                  s -= 4
                  b += 2

                }
              }
            }
          }

          // Powerups

          if(PlayerScore <= 40 && PlayerLives <= 700) {
            enbullets += newEnemySwarm.invi()

          }

          //Intersection
          for (i <- enbullets) { // Player and Enemy Bullets
            if (i.checkintersects(player) == true) {
              player.stop()
              PlayerLives -= 1

            }
          }

          for (i <- bullets) { // Player Bullets and Enemies
            for (j <- newEnemySwarm.swarm) {
              if (i.checkintersects(j) == true) {
                j.move(new Vec2(500, 600))
                explodes.ScreenPos = i.pos
                explodes.display(g)
                EnemySwarm += j
                PlayerScore += 1

              }
            }
          }

          for (i <- bullets) {
            if (Boss.checkintersects(i) == true) {
              BossHp -= 10
              explodes.ScreenPos = i.pos

            }
          }

          for (i <- newEnemySwarm.swarm) { // Player and Enemy
            if (i.checkintersects(player) == true) {
              PlayerLives -= 100

            }
          }



          // Sound




          // Boss Display and Powerups
          var BossMovement = 0

          if (EnemySwarm.length == 21) {
            println(EnemySwarm.length + " " + (math.random * 3).toInt)
            BossBullets.foreach { x => x.timeStep() }
            BossBullets.foreach { x => x.display(g) }
            if (swarmTimer % 3 == 0) {
              BossBullets += Boss.shoot()
              BossBullets += Boss.shoot2()

            }
            Boss.display(g)
            for (i <- 1 to 500) {
              BossMovement += i
              if (BossMovement % 6 == 0) {

              }
            }
          } else {
            println(EnemySwarm.length + " " + math.random * 5)
            ResetSwarm()
          }
          if (BossHp <= 0) {
            ResetSwarm()
            BossHp += 400
          }

          if (PlayerLives <= 0) {
            Endscreen = true
            Startscreen = false
          }

          if (timers <= 0) {
            Endscreen = false

          }

          if (Startscreen == true && Endscreen == false) {
            g.clearRect(0, 0, 400, 600)
            background2.display(g)

            content = canvas
            canvas.onKeyPressed = (e: KeyEvent) => {
              if (e.code.toString == "ENTER") {
                Startscreen = false
                Endscreen = false
                println(Startscreen)
                println(Endscreen)
                println("R Key was Pressed")
              } else {
                if (e.code.toString == "2") {
                  twoPlayer = true
                }

              }
            }

          } else if (Endscreen == true && Startscreen == false) {
            background4.display(g)

            g.fillText("Looks Like You Lost", 150, 200)
            g.fillText("Player Score" + " " + PlayerScore.toString(), 150, 10)

            //g.fillText("Game Will Close in:" + " " + timers.toString(), 130, 160)

            content = canvas

          }

        }
        canvas.requestFocus()

      })
      timer.start()

    }
  }
}
  
