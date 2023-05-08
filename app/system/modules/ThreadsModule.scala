package system.modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule

class ThreadsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {}

//  @Provides
//  @Singleton
//  @Named("dbExContext")
//  def dbExContext(akka: ActorSystem): ExecutionContext = akka.dispatchers.lookup("threads.db.dispatcher")
}
