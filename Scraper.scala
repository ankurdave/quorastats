import java.net.URL

import org.lobobrowser.html.domimpl.{HTMLDocumentImpl, HTMLAbstractUIElement, DocumentNotificationAdapter}
import org.lobobrowser.html.parser.{DocumentBuilderImpl, InputSourceImpl}
import org.lobobrowser.html.test.SimpleUserAgentContext
import org.lobobrowser.html.js.{Executor, Event}

import org.w3c.dom.{Node, Element}

object Scraper {
  def scrapeAnswers(url: String): Seq[Answer] = {
    val uac = new SimpleUserAgentContext()
    uac.setExternalCSSEnabled(false)
    val dbi = new DocumentBuilderImpl(uac)
    val in = new URL(url).openConnection().getInputStream()
    val doc = dbi.createDocument(new InputSourceImpl(in, url, "UTF-8")).asInstanceOf[HTMLDocumentImpl]
    println("Loading document")
    doc.addDocumentNotificationListener(new DocumentNotificationAdapter {
      def externalScriptLoading(node: Node) {
        println(node.toString())
      }
    })
    doc.load()

    println("Clicking button")
    // Click the "pager_next" button
    val nextButtons = doc.getElementsByTagName("div")
    for {
      i <- 0 until nextButtons.getLength()
      nextButton = nextButtons.item(i)
      if hasClass(nextButton, "pager_next")
      next = nextButton.asInstanceOf[HTMLAbstractUIElement]
      if next != null
    } {
      Executor.executeFunction(next, next.getOnclick(), new Event("onclick", next))
    }
    println("Done")
    
    val nodes = doc.getElementsByTagName("div")
    for {
      i <- 0 until nodes.getLength()
      node = nodes.item(i)
      if hasClass(node, "feed_item_answer")
      voterCount <- findChildrenWithClass(node, "strong", "voter_count")
      permalinkNode <- findChildrenWithClass(node, "a", "answer_permalink")
      permalink <- getAttribute(permalinkNode, "href")
    } yield Answer(permalink, voterCount.getTextContent().toInt)
  }

  def getAttribute(node: Node, attr: String): Option[java.lang.String] = {
    if (node.getAttributes().getNamedItem(attr) != null)
      Some(node.getAttributes().getNamedItem(attr).getNodeValue())
    else
      None
  }

  def hasClass(node: Node, cls: String): Boolean =
    getAttribute(node, "class") match {
      case Some(c) => c.split("\\s+").exists(_ equalsIgnoreCase cls)
      case None => false
    }

  def findChildrenWithClass(node: Node, tagName: String, cls: String): Seq[Node] =
    node.getNodeType() match {
      case Node.ELEMENT_NODE =>
        val children = node.asInstanceOf[Element].getElementsByTagName(tagName)
        for {
          i <- 0 until children.getLength()
          child = children.item(i)
          if hasClass(child, cls)
        } yield child
      case _ => List()
    }

  def main(args: Array[String]) {
    for (answer <- scrapeAnswers(args(0))) {
      println("%s: %d votes".format(answer.permalink, answer.votes))
    }
  }
}

case class Answer(val permalink: String, val votes: Int)
