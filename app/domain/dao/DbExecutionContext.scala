package domain.dao

import akka.actor.ActorSystem
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.libs.concurrent.CustomExecutionContext

@Singleton
@Named("dbExContext")
class DbExecutionContext @Inject() (actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "threads.db.dispatcher")
