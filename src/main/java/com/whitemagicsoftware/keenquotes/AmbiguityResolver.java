/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.whitemagicsoftware.keenquotes.TokenType.*;
import static java.lang.String.valueOf;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

/**
 * Responsible for resolving ambiguous quotes that could not be resolved during
 * a first-pass parse of a document. Creates an ordered sequence of tokens that
 * can be used to resolve certain ambiguities due to the fact that nested
 * quotations must alternate between double and single quotes. This means that
 * a string such as, "Is Iris' name Greek?" can have its single quote resolved
 * (because the apostrophe is alone within double quotes).
 */
public final class AmbiguityResolver implements Consumer<Token> {

  private final Consumer<Token> mConsumer;
  private final List<Token> mTokens = new ArrayList<>();
  private Tree<Token> mTree = new Tree<>();

  public AmbiguityResolver( final Consumer<Token> consumer ) {
    assert consumer != null;
    mConsumer = consumer;
  }

  public static void analyze(
    final String text,
    final Contractions contractions,
    final Consumer<Token> consumer ) {
    final var resolver = new AmbiguityResolver( consumer );

    QuoteEmitter.analyze( text, contractions, resolver );
    resolver.visit();
  }

  /**
   * Accepts only opening, closing, and ambiguous {@link Token} types.
   *
   * @param token the input argument
   */
  @Override
  public void accept( final Token token ) {
    // Create a new subtree when an opening quotation mark is found.
    if( token.isType( QUOTE_OPENING_SINGLE ) ||
      token.isType( QUOTE_OPENING_DOUBLE ) ) {
      mTree = mTree.opening( token );
    }
    // Close the subtree if it was open, try to close it.
    else if( token.isType( QUOTE_CLOSING_SINGLE ) ||
      token.isType( QUOTE_CLOSING_DOUBLE ) ) {
      mTree = mTree.closing( token );
    }
    // Add any ambiguous tokens to the subtree, which will be resolved after
    // the in-memory AST is built.
    else if( token.isAmbiguous() ) {
      mTree.add( token );
    }
  }

  /**
   * Traverse the tree and resolve as many ambiguous tokens as possible. This
   * is called after the document's AST is built.
   */
  private void visit() {
    Tree<Token> parent;

    // Search for the tree's root.
    while( (parent = mTree.parent()) != null ) {
      mTree = parent;
    }

    visit( mTree );

    // Show me the money.
    System.out.println( format( mTree.toXml() ) );
  }

  private void visit( final Tree<Token> tree ) {
    final var queue = new LinkedList<Tree<Token>>();
    queue.add( tree );

    while( !queue.isEmpty() ) {
      final var current = queue.poll();

      resolve( current );

      queue.addAll( current.subtrees() );
    }
  }

  /**
   * When a tree:
   *
   * <ul>
   *   <li>has no closing single quote and no laggards, then all leaders are
   *   apostrophes;</li>
   *   <li>has no opening single quote and no leaders, then all laggards are
   *   apostrophes;</li>
   *   <li>is balanced and has no leaders, then all laggards are apostrophes;
   *   </li>
   *   <li>is balanced and has no laggards, then all leaders are apostrophes;
   *   </li>
   *   <li>has one closing single quote, no laggards, and one leader, then
   *   the leader is an opening single quote; and</li>
   *   <li>has one opening single quote, no leaders, and one laggard, then
   *   the laggard is a closing single quote.</li>
   * </ul>
   *
   * @param tree The {@link Tree} that may contain ambiguous tokens to resolve.
   */
  private void resolve( final Tree<Token> tree ) {
    if( tree.hasOpeningSingleQuote() && !tree.hasClosingSingleQuote() &&
      tree.count( QUOTE_AMBIGUOUS_LAGGING ) == 0 ) {
      tree.replaceAll( QUOTE_AMBIGUOUS_LEADING, QUOTE_APOSTROPHE );
    }

    if( !tree.hasOpeningSingleQuote() && tree.hasClosingSingleQuote() &&
      tree.count( QUOTE_AMBIGUOUS_LEADING ) == 0 ) {
      tree.replaceAll( QUOTE_AMBIGUOUS_LAGGING, QUOTE_APOSTROPHE );
    }

    if( !tree.hasOpeningSingleQuote() && !tree.hasClosingSingleQuote() &&
      tree.count( QUOTE_AMBIGUOUS_LEADING ) == 1 &&
      tree.count( QUOTE_AMBIGUOUS_LAGGING ) == 0 ) {
      tree.replaceAll( QUOTE_AMBIGUOUS_LEADING, QUOTE_APOSTROPHE );
    }

    if( !tree.hasOpeningSingleQuote() && !tree.hasClosingSingleQuote() &&
      tree.count( QUOTE_AMBIGUOUS_LEADING ) == 0 &&
      tree.count( QUOTE_AMBIGUOUS_LAGGING ) == 1 ) {
      tree.replaceAll( QUOTE_AMBIGUOUS_LAGGING, QUOTE_APOSTROPHE );
    }

//    if( tree.isBalanced() &&
//      tree.count( QUOTE_AMBIGUOUS_LEADING ) == 0 ) {
//      tree.replaceAll( QUOTE_AMBIGUOUS_LAGGING, QUOTE_APOSTROPHE );
//    }
//
//    if( tree.isBalanced() &&
//      tree.count( QUOTE_AMBIGUOUS_LAGGING ) == 0 ) {
//      tree.replaceAll( QUOTE_AMBIGUOUS_LEADING, QUOTE_APOSTROPHE );
//    }
//
//    if(
//      tree.count( QUOTE_OPENING_SINGLE ) == 1 &&
//        tree.count( QUOTE_AMBIGUOUS_LEADING ) == 0 &&
//        tree.count( QUOTE_AMBIGUOUS_LAGGING ) == 1 ) {
//      tree.replaceAll( QUOTE_AMBIGUOUS_LAGGING, QUOTE_CLOSING_SINGLE );
//    }
//
//    if( tree.count( QUOTE_CLOSING_SINGLE ) == 1 &&
//      tree.count( QUOTE_AMBIGUOUS_LAGGING ) == 0 &&
//      tree.count( QUOTE_AMBIGUOUS_LEADING ) == 1 ) {
//      tree.replaceAll( QUOTE_AMBIGUOUS_LEADING, QUOTE_OPENING_SINGLE );
//    }
  }

  private static String format( final String xml ) {
    try {
      final var dbf = DocumentBuilderFactory.newInstance();
      final var db = dbf.newDocumentBuilder();
      final var is = new InputSource( new StringReader( xml ) );
      final var source = new DOMSource( db.parse( is ) );
      final var result = new StreamResult( new StringWriter() );
      final var t = TransformerFactory.newInstance().newTransformer();

      t.setOutputProperty( OMIT_XML_DECLARATION, "yes" );
      t.setOutputProperty( INDENT, "yes" );
      t.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount",
                           valueOf( 2 ) );
      t.transform( source, result );

      return result.getWriter().toString();
    } catch( final Exception e ) {
      return xml;
    }
  }
}
