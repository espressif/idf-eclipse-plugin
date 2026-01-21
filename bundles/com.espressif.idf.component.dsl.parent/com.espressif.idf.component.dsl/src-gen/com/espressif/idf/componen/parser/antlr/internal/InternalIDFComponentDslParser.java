package com.espressif.idf.componen.parser.antlr.internal;

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import com.espressif.idf.componen.services.IDFComponentDslGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalIDFComponentDslParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_STRING", "RULE_ID", "RULE_INT", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'description:'", "'version:'", "'url:'", "'#'", "'dependencies:'", "'idf:'", "':'", "'public:'", "'override_path:'", "'rules:'"
    };
    public static final int RULE_STRING=4;
    public static final int RULE_SL_COMMENT=8;
    public static final int T__19=19;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__11=11;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int EOF=-1;
    public static final int RULE_ID=5;
    public static final int RULE_WS=9;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_INT=6;
    public static final int RULE_ML_COMMENT=7;
    public static final int T__20=20;

    // delegates
    // delegators


        public InternalIDFComponentDslParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalIDFComponentDslParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalIDFComponentDslParser.tokenNames; }
    public String getGrammarFileName() { return "InternalIDFComponentDsl.g"; }



     	private IDFComponentDslGrammarAccess grammarAccess;

        public InternalIDFComponentDslParser(TokenStream input, IDFComponentDslGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }

        @Override
        protected String getFirstRuleName() {
        	return "ComponentModel";
       	}

       	@Override
       	protected IDFComponentDslGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}




    // $ANTLR start "entryRuleComponentModel"
    // InternalIDFComponentDsl.g:64:1: entryRuleComponentModel returns [EObject current=null] : iv_ruleComponentModel= ruleComponentModel EOF ;
    public final EObject entryRuleComponentModel() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleComponentModel = null;


        try {
            // InternalIDFComponentDsl.g:64:55: (iv_ruleComponentModel= ruleComponentModel EOF )
            // InternalIDFComponentDsl.g:65:2: iv_ruleComponentModel= ruleComponentModel EOF
            {
             newCompositeNode(grammarAccess.getComponentModelRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleComponentModel=ruleComponentModel();

            state._fsp--;

             current =iv_ruleComponentModel; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleComponentModel"


    // $ANTLR start "ruleComponentModel"
    // InternalIDFComponentDsl.g:71:1: ruleComponentModel returns [EObject current=null] : ( (lv_elements_0_0= ruleType ) )* ;
    public final EObject ruleComponentModel() throws RecognitionException {
        EObject current = null;

        EObject lv_elements_0_0 = null;



        	enterRule();

        try {
            // InternalIDFComponentDsl.g:77:2: ( ( (lv_elements_0_0= ruleType ) )* )
            // InternalIDFComponentDsl.g:78:2: ( (lv_elements_0_0= ruleType ) )*
            {
            // InternalIDFComponentDsl.g:78:2: ( (lv_elements_0_0= ruleType ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=11 && LA1_0<=15)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // InternalIDFComponentDsl.g:79:3: (lv_elements_0_0= ruleType )
            	    {
            	    // InternalIDFComponentDsl.g:79:3: (lv_elements_0_0= ruleType )
            	    // InternalIDFComponentDsl.g:80:4: lv_elements_0_0= ruleType
            	    {

            	    				newCompositeNode(grammarAccess.getComponentModelAccess().getElementsTypeParserRuleCall_0());
            	    			
            	    pushFollow(FOLLOW_3);
            	    lv_elements_0_0=ruleType();

            	    state._fsp--;


            	    				if (current==null) {
            	    					current = createModelElementForParent(grammarAccess.getComponentModelRule());
            	    				}
            	    				add(
            	    					current,
            	    					"elements",
            	    					lv_elements_0_0,
            	    					"com.espressif.idf.componen.IDFComponentDsl.Type");
            	    				afterParserOrEnumRuleCall();
            	    			

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleComponentModel"


    // $ANTLR start "entryRuleType"
    // InternalIDFComponentDsl.g:100:1: entryRuleType returns [EObject current=null] : iv_ruleType= ruleType EOF ;
    public final EObject entryRuleType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleType = null;


        try {
            // InternalIDFComponentDsl.g:100:45: (iv_ruleType= ruleType EOF )
            // InternalIDFComponentDsl.g:101:2: iv_ruleType= ruleType EOF
            {
             newCompositeNode(grammarAccess.getTypeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleType=ruleType();

            state._fsp--;

             current =iv_ruleType; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleType"


    // $ANTLR start "ruleType"
    // InternalIDFComponentDsl.g:107:1: ruleType returns [EObject current=null] : (this_Description_0= ruleDescription | this_VersionInfo_1= ruleVersionInfo | this_DependenciesComp_2= ruleDependenciesComp | this_URL_3= ruleURL | this_COMMENT_4= ruleCOMMENT ) ;
    public final EObject ruleType() throws RecognitionException {
        EObject current = null;

        EObject this_Description_0 = null;

        EObject this_VersionInfo_1 = null;

        EObject this_DependenciesComp_2 = null;

        EObject this_URL_3 = null;

        EObject this_COMMENT_4 = null;



        	enterRule();

        try {
            // InternalIDFComponentDsl.g:113:2: ( (this_Description_0= ruleDescription | this_VersionInfo_1= ruleVersionInfo | this_DependenciesComp_2= ruleDependenciesComp | this_URL_3= ruleURL | this_COMMENT_4= ruleCOMMENT ) )
            // InternalIDFComponentDsl.g:114:2: (this_Description_0= ruleDescription | this_VersionInfo_1= ruleVersionInfo | this_DependenciesComp_2= ruleDependenciesComp | this_URL_3= ruleURL | this_COMMENT_4= ruleCOMMENT )
            {
            // InternalIDFComponentDsl.g:114:2: (this_Description_0= ruleDescription | this_VersionInfo_1= ruleVersionInfo | this_DependenciesComp_2= ruleDependenciesComp | this_URL_3= ruleURL | this_COMMENT_4= ruleCOMMENT )
            int alt2=5;
            switch ( input.LA(1) ) {
            case 11:
                {
                alt2=1;
                }
                break;
            case 12:
                {
                alt2=2;
                }
                break;
            case 15:
                {
                alt2=3;
                }
                break;
            case 13:
                {
                alt2=4;
                }
                break;
            case 14:
                {
                alt2=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // InternalIDFComponentDsl.g:115:3: this_Description_0= ruleDescription
                    {

                    			newCompositeNode(grammarAccess.getTypeAccess().getDescriptionParserRuleCall_0());
                    		
                    pushFollow(FOLLOW_2);
                    this_Description_0=ruleDescription();

                    state._fsp--;


                    			current = this_Description_0;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 2 :
                    // InternalIDFComponentDsl.g:124:3: this_VersionInfo_1= ruleVersionInfo
                    {

                    			newCompositeNode(grammarAccess.getTypeAccess().getVersionInfoParserRuleCall_1());
                    		
                    pushFollow(FOLLOW_2);
                    this_VersionInfo_1=ruleVersionInfo();

                    state._fsp--;


                    			current = this_VersionInfo_1;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 3 :
                    // InternalIDFComponentDsl.g:133:3: this_DependenciesComp_2= ruleDependenciesComp
                    {

                    			newCompositeNode(grammarAccess.getTypeAccess().getDependenciesCompParserRuleCall_2());
                    		
                    pushFollow(FOLLOW_2);
                    this_DependenciesComp_2=ruleDependenciesComp();

                    state._fsp--;


                    			current = this_DependenciesComp_2;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 4 :
                    // InternalIDFComponentDsl.g:142:3: this_URL_3= ruleURL
                    {

                    			newCompositeNode(grammarAccess.getTypeAccess().getURLParserRuleCall_3());
                    		
                    pushFollow(FOLLOW_2);
                    this_URL_3=ruleURL();

                    state._fsp--;


                    			current = this_URL_3;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 5 :
                    // InternalIDFComponentDsl.g:151:3: this_COMMENT_4= ruleCOMMENT
                    {

                    			newCompositeNode(grammarAccess.getTypeAccess().getCOMMENTParserRuleCall_4());
                    		
                    pushFollow(FOLLOW_2);
                    this_COMMENT_4=ruleCOMMENT();

                    state._fsp--;


                    			current = this_COMMENT_4;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleType"


    // $ANTLR start "entryRuleDescription"
    // InternalIDFComponentDsl.g:163:1: entryRuleDescription returns [EObject current=null] : iv_ruleDescription= ruleDescription EOF ;
    public final EObject entryRuleDescription() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDescription = null;


        try {
            // InternalIDFComponentDsl.g:163:52: (iv_ruleDescription= ruleDescription EOF )
            // InternalIDFComponentDsl.g:164:2: iv_ruleDescription= ruleDescription EOF
            {
             newCompositeNode(grammarAccess.getDescriptionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleDescription=ruleDescription();

            state._fsp--;

             current =iv_ruleDescription; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDescription"


    // $ANTLR start "ruleDescription"
    // InternalIDFComponentDsl.g:170:1: ruleDescription returns [EObject current=null] : (otherlv_0= 'description:' ( (lv_name_1_0= RULE_STRING ) ) ) ;
    public final EObject ruleDescription() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalIDFComponentDsl.g:176:2: ( (otherlv_0= 'description:' ( (lv_name_1_0= RULE_STRING ) ) ) )
            // InternalIDFComponentDsl.g:177:2: (otherlv_0= 'description:' ( (lv_name_1_0= RULE_STRING ) ) )
            {
            // InternalIDFComponentDsl.g:177:2: (otherlv_0= 'description:' ( (lv_name_1_0= RULE_STRING ) ) )
            // InternalIDFComponentDsl.g:178:3: otherlv_0= 'description:' ( (lv_name_1_0= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,11,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getDescriptionAccess().getDescriptionKeyword_0());
            		
            // InternalIDFComponentDsl.g:182:3: ( (lv_name_1_0= RULE_STRING ) )
            // InternalIDFComponentDsl.g:183:4: (lv_name_1_0= RULE_STRING )
            {
            // InternalIDFComponentDsl.g:183:4: (lv_name_1_0= RULE_STRING )
            // InternalIDFComponentDsl.g:184:5: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            					newLeafNode(lv_name_1_0, grammarAccess.getDescriptionAccess().getNameSTRINGTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDescriptionRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDescription"


    // $ANTLR start "entryRuleVersionInfo"
    // InternalIDFComponentDsl.g:204:1: entryRuleVersionInfo returns [EObject current=null] : iv_ruleVersionInfo= ruleVersionInfo EOF ;
    public final EObject entryRuleVersionInfo() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVersionInfo = null;


        try {
            // InternalIDFComponentDsl.g:204:52: (iv_ruleVersionInfo= ruleVersionInfo EOF )
            // InternalIDFComponentDsl.g:205:2: iv_ruleVersionInfo= ruleVersionInfo EOF
            {
             newCompositeNode(grammarAccess.getVersionInfoRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleVersionInfo=ruleVersionInfo();

            state._fsp--;

             current =iv_ruleVersionInfo; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleVersionInfo"


    // $ANTLR start "ruleVersionInfo"
    // InternalIDFComponentDsl.g:211:1: ruleVersionInfo returns [EObject current=null] : (otherlv_0= 'version:' ( (lv_name_1_0= RULE_STRING ) ) ) ;
    public final EObject ruleVersionInfo() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalIDFComponentDsl.g:217:2: ( (otherlv_0= 'version:' ( (lv_name_1_0= RULE_STRING ) ) ) )
            // InternalIDFComponentDsl.g:218:2: (otherlv_0= 'version:' ( (lv_name_1_0= RULE_STRING ) ) )
            {
            // InternalIDFComponentDsl.g:218:2: (otherlv_0= 'version:' ( (lv_name_1_0= RULE_STRING ) ) )
            // InternalIDFComponentDsl.g:219:3: otherlv_0= 'version:' ( (lv_name_1_0= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,12,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getVersionInfoAccess().getVersionKeyword_0());
            		
            // InternalIDFComponentDsl.g:223:3: ( (lv_name_1_0= RULE_STRING ) )
            // InternalIDFComponentDsl.g:224:4: (lv_name_1_0= RULE_STRING )
            {
            // InternalIDFComponentDsl.g:224:4: (lv_name_1_0= RULE_STRING )
            // InternalIDFComponentDsl.g:225:5: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            					newLeafNode(lv_name_1_0, grammarAccess.getVersionInfoAccess().getNameSTRINGTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getVersionInfoRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleVersionInfo"


    // $ANTLR start "entryRuleURL"
    // InternalIDFComponentDsl.g:245:1: entryRuleURL returns [EObject current=null] : iv_ruleURL= ruleURL EOF ;
    public final EObject entryRuleURL() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleURL = null;


        try {
            // InternalIDFComponentDsl.g:245:44: (iv_ruleURL= ruleURL EOF )
            // InternalIDFComponentDsl.g:246:2: iv_ruleURL= ruleURL EOF
            {
             newCompositeNode(grammarAccess.getURLRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleURL=ruleURL();

            state._fsp--;

             current =iv_ruleURL; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleURL"


    // $ANTLR start "ruleURL"
    // InternalIDFComponentDsl.g:252:1: ruleURL returns [EObject current=null] : (otherlv_0= 'url:' ( (lv_name_1_0= RULE_STRING ) ) ) ;
    public final EObject ruleURL() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalIDFComponentDsl.g:258:2: ( (otherlv_0= 'url:' ( (lv_name_1_0= RULE_STRING ) ) ) )
            // InternalIDFComponentDsl.g:259:2: (otherlv_0= 'url:' ( (lv_name_1_0= RULE_STRING ) ) )
            {
            // InternalIDFComponentDsl.g:259:2: (otherlv_0= 'url:' ( (lv_name_1_0= RULE_STRING ) ) )
            // InternalIDFComponentDsl.g:260:3: otherlv_0= 'url:' ( (lv_name_1_0= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,13,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getURLAccess().getUrlKeyword_0());
            		
            // InternalIDFComponentDsl.g:264:3: ( (lv_name_1_0= RULE_STRING ) )
            // InternalIDFComponentDsl.g:265:4: (lv_name_1_0= RULE_STRING )
            {
            // InternalIDFComponentDsl.g:265:4: (lv_name_1_0= RULE_STRING )
            // InternalIDFComponentDsl.g:266:5: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            					newLeafNode(lv_name_1_0, grammarAccess.getURLAccess().getNameSTRINGTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getURLRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleURL"


    // $ANTLR start "entryRuleCOMMENT"
    // InternalIDFComponentDsl.g:286:1: entryRuleCOMMENT returns [EObject current=null] : iv_ruleCOMMENT= ruleCOMMENT EOF ;
    public final EObject entryRuleCOMMENT() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCOMMENT = null;


        try {
            // InternalIDFComponentDsl.g:286:48: (iv_ruleCOMMENT= ruleCOMMENT EOF )
            // InternalIDFComponentDsl.g:287:2: iv_ruleCOMMENT= ruleCOMMENT EOF
            {
             newCompositeNode(grammarAccess.getCOMMENTRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleCOMMENT=ruleCOMMENT();

            state._fsp--;

             current =iv_ruleCOMMENT; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCOMMENT"


    // $ANTLR start "ruleCOMMENT"
    // InternalIDFComponentDsl.g:293:1: ruleCOMMENT returns [EObject current=null] : (otherlv_0= '#' ( (lv_name_1_0= RULE_ID ) )* ) ;
    public final EObject ruleCOMMENT() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalIDFComponentDsl.g:299:2: ( (otherlv_0= '#' ( (lv_name_1_0= RULE_ID ) )* ) )
            // InternalIDFComponentDsl.g:300:2: (otherlv_0= '#' ( (lv_name_1_0= RULE_ID ) )* )
            {
            // InternalIDFComponentDsl.g:300:2: (otherlv_0= '#' ( (lv_name_1_0= RULE_ID ) )* )
            // InternalIDFComponentDsl.g:301:3: otherlv_0= '#' ( (lv_name_1_0= RULE_ID ) )*
            {
            otherlv_0=(Token)match(input,14,FOLLOW_5); 

            			newLeafNode(otherlv_0, grammarAccess.getCOMMENTAccess().getNumberSignKeyword_0());
            		
            // InternalIDFComponentDsl.g:305:3: ( (lv_name_1_0= RULE_ID ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==RULE_ID) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // InternalIDFComponentDsl.g:306:4: (lv_name_1_0= RULE_ID )
            	    {
            	    // InternalIDFComponentDsl.g:306:4: (lv_name_1_0= RULE_ID )
            	    // InternalIDFComponentDsl.g:307:5: lv_name_1_0= RULE_ID
            	    {
            	    lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_5); 

            	    					newLeafNode(lv_name_1_0, grammarAccess.getCOMMENTAccess().getNameIDTerminalRuleCall_1_0());
            	    				

            	    					if (current==null) {
            	    						current = createModelElement(grammarAccess.getCOMMENTRule());
            	    					}
            	    					setWithLastConsumed(
            	    						current,
            	    						"name",
            	    						lv_name_1_0,
            	    						"org.eclipse.xtext.common.Terminals.ID");
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCOMMENT"


    // $ANTLR start "entryRuleDependenciesComp"
    // InternalIDFComponentDsl.g:327:1: entryRuleDependenciesComp returns [EObject current=null] : iv_ruleDependenciesComp= ruleDependenciesComp EOF ;
    public final EObject entryRuleDependenciesComp() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDependenciesComp = null;


        try {
            // InternalIDFComponentDsl.g:327:57: (iv_ruleDependenciesComp= ruleDependenciesComp EOF )
            // InternalIDFComponentDsl.g:328:2: iv_ruleDependenciesComp= ruleDependenciesComp EOF
            {
             newCompositeNode(grammarAccess.getDependenciesCompRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleDependenciesComp=ruleDependenciesComp();

            state._fsp--;

             current =iv_ruleDependenciesComp; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDependenciesComp"


    // $ANTLR start "ruleDependenciesComp"
    // InternalIDFComponentDsl.g:334:1: ruleDependenciesComp returns [EObject current=null] : (otherlv_0= 'dependencies:' ( (lv_features_1_0= ruleFeature ) )* ) ;
    public final EObject ruleDependenciesComp() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        EObject lv_features_1_0 = null;



        	enterRule();

        try {
            // InternalIDFComponentDsl.g:340:2: ( (otherlv_0= 'dependencies:' ( (lv_features_1_0= ruleFeature ) )* ) )
            // InternalIDFComponentDsl.g:341:2: (otherlv_0= 'dependencies:' ( (lv_features_1_0= ruleFeature ) )* )
            {
            // InternalIDFComponentDsl.g:341:2: (otherlv_0= 'dependencies:' ( (lv_features_1_0= ruleFeature ) )* )
            // InternalIDFComponentDsl.g:342:3: otherlv_0= 'dependencies:' ( (lv_features_1_0= ruleFeature ) )*
            {
            otherlv_0=(Token)match(input,15,FOLLOW_6); 

            			newLeafNode(otherlv_0, grammarAccess.getDependenciesCompAccess().getDependenciesKeyword_0());
            		
            // InternalIDFComponentDsl.g:346:3: ( (lv_features_1_0= ruleFeature ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==RULE_ID||LA4_0==16||LA4_0==19) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // InternalIDFComponentDsl.g:347:4: (lv_features_1_0= ruleFeature )
            	    {
            	    // InternalIDFComponentDsl.g:347:4: (lv_features_1_0= ruleFeature )
            	    // InternalIDFComponentDsl.g:348:5: lv_features_1_0= ruleFeature
            	    {

            	    					newCompositeNode(grammarAccess.getDependenciesCompAccess().getFeaturesFeatureParserRuleCall_1_0());
            	    				
            	    pushFollow(FOLLOW_6);
            	    lv_features_1_0=ruleFeature();

            	    state._fsp--;


            	    					if (current==null) {
            	    						current = createModelElementForParent(grammarAccess.getDependenciesCompRule());
            	    					}
            	    					add(
            	    						current,
            	    						"features",
            	    						lv_features_1_0,
            	    						"com.espressif.idf.componen.IDFComponentDsl.Feature");
            	    					afterParserOrEnumRuleCall();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDependenciesComp"


    // $ANTLR start "entryRuleFeature"
    // InternalIDFComponentDsl.g:369:1: entryRuleFeature returns [EObject current=null] : iv_ruleFeature= ruleFeature EOF ;
    public final EObject entryRuleFeature() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFeature = null;


        try {
            // InternalIDFComponentDsl.g:369:48: (iv_ruleFeature= ruleFeature EOF )
            // InternalIDFComponentDsl.g:370:2: iv_ruleFeature= ruleFeature EOF
            {
             newCompositeNode(grammarAccess.getFeatureRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleFeature=ruleFeature();

            state._fsp--;

             current =iv_ruleFeature; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFeature"


    // $ANTLR start "ruleFeature"
    // InternalIDFComponentDsl.g:376:1: ruleFeature returns [EObject current=null] : (this_IDF_0= ruleIDF | this_OVERRIDE_PATH_1= ruleOVERRIDE_PATH | this_COMPONENT_NAME_2= ruleCOMPONENT_NAME ) ;
    public final EObject ruleFeature() throws RecognitionException {
        EObject current = null;

        EObject this_IDF_0 = null;

        EObject this_OVERRIDE_PATH_1 = null;

        EObject this_COMPONENT_NAME_2 = null;



        	enterRule();

        try {
            // InternalIDFComponentDsl.g:382:2: ( (this_IDF_0= ruleIDF | this_OVERRIDE_PATH_1= ruleOVERRIDE_PATH | this_COMPONENT_NAME_2= ruleCOMPONENT_NAME ) )
            // InternalIDFComponentDsl.g:383:2: (this_IDF_0= ruleIDF | this_OVERRIDE_PATH_1= ruleOVERRIDE_PATH | this_COMPONENT_NAME_2= ruleCOMPONENT_NAME )
            {
            // InternalIDFComponentDsl.g:383:2: (this_IDF_0= ruleIDF | this_OVERRIDE_PATH_1= ruleOVERRIDE_PATH | this_COMPONENT_NAME_2= ruleCOMPONENT_NAME )
            int alt5=3;
            switch ( input.LA(1) ) {
            case 16:
                {
                alt5=1;
                }
                break;
            case 19:
                {
                alt5=2;
                }
                break;
            case RULE_ID:
                {
                alt5=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // InternalIDFComponentDsl.g:384:3: this_IDF_0= ruleIDF
                    {

                    			newCompositeNode(grammarAccess.getFeatureAccess().getIDFParserRuleCall_0());
                    		
                    pushFollow(FOLLOW_2);
                    this_IDF_0=ruleIDF();

                    state._fsp--;


                    			current = this_IDF_0;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 2 :
                    // InternalIDFComponentDsl.g:393:3: this_OVERRIDE_PATH_1= ruleOVERRIDE_PATH
                    {

                    			newCompositeNode(grammarAccess.getFeatureAccess().getOVERRIDE_PATHParserRuleCall_1());
                    		
                    pushFollow(FOLLOW_2);
                    this_OVERRIDE_PATH_1=ruleOVERRIDE_PATH();

                    state._fsp--;


                    			current = this_OVERRIDE_PATH_1;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 3 :
                    // InternalIDFComponentDsl.g:402:3: this_COMPONENT_NAME_2= ruleCOMPONENT_NAME
                    {

                    			newCompositeNode(grammarAccess.getFeatureAccess().getCOMPONENT_NAMEParserRuleCall_2());
                    		
                    pushFollow(FOLLOW_2);
                    this_COMPONENT_NAME_2=ruleCOMPONENT_NAME();

                    state._fsp--;


                    			current = this_COMPONENT_NAME_2;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFeature"


    // $ANTLR start "entryRuleIDF"
    // InternalIDFComponentDsl.g:414:1: entryRuleIDF returns [EObject current=null] : iv_ruleIDF= ruleIDF EOF ;
    public final EObject entryRuleIDF() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleIDF = null;


        try {
            // InternalIDFComponentDsl.g:414:44: (iv_ruleIDF= ruleIDF EOF )
            // InternalIDFComponentDsl.g:415:2: iv_ruleIDF= ruleIDF EOF
            {
             newCompositeNode(grammarAccess.getIDFRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleIDF=ruleIDF();

            state._fsp--;

             current =iv_ruleIDF; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleIDF"


    // $ANTLR start "ruleIDF"
    // InternalIDFComponentDsl.g:421:1: ruleIDF returns [EObject current=null] : (otherlv_0= 'idf:' ( (lv_name_1_0= RULE_STRING ) ) ) ;
    public final EObject ruleIDF() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalIDFComponentDsl.g:427:2: ( (otherlv_0= 'idf:' ( (lv_name_1_0= RULE_STRING ) ) ) )
            // InternalIDFComponentDsl.g:428:2: (otherlv_0= 'idf:' ( (lv_name_1_0= RULE_STRING ) ) )
            {
            // InternalIDFComponentDsl.g:428:2: (otherlv_0= 'idf:' ( (lv_name_1_0= RULE_STRING ) ) )
            // InternalIDFComponentDsl.g:429:3: otherlv_0= 'idf:' ( (lv_name_1_0= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,16,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getIDFAccess().getIdfKeyword_0());
            		
            // InternalIDFComponentDsl.g:433:3: ( (lv_name_1_0= RULE_STRING ) )
            // InternalIDFComponentDsl.g:434:4: (lv_name_1_0= RULE_STRING )
            {
            // InternalIDFComponentDsl.g:434:4: (lv_name_1_0= RULE_STRING )
            // InternalIDFComponentDsl.g:435:5: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            					newLeafNode(lv_name_1_0, grammarAccess.getIDFAccess().getNameSTRINGTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getIDFRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleIDF"


    // $ANTLR start "entryRuleCOMPONENT_NAME"
    // InternalIDFComponentDsl.g:455:1: entryRuleCOMPONENT_NAME returns [EObject current=null] : iv_ruleCOMPONENT_NAME= ruleCOMPONENT_NAME EOF ;
    public final EObject entryRuleCOMPONENT_NAME() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCOMPONENT_NAME = null;


        try {
            // InternalIDFComponentDsl.g:455:55: (iv_ruleCOMPONENT_NAME= ruleCOMPONENT_NAME EOF )
            // InternalIDFComponentDsl.g:456:2: iv_ruleCOMPONENT_NAME= ruleCOMPONENT_NAME EOF
            {
             newCompositeNode(grammarAccess.getCOMPONENT_NAMERule()); 
            pushFollow(FOLLOW_1);
            iv_ruleCOMPONENT_NAME=ruleCOMPONENT_NAME();

            state._fsp--;

             current =iv_ruleCOMPONENT_NAME; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCOMPONENT_NAME"


    // $ANTLR start "ruleCOMPONENT_NAME"
    // InternalIDFComponentDsl.g:462:1: ruleCOMPONENT_NAME returns [EObject current=null] : ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= ':' ( (lv_features_2_0= ruleCOMPONENT_NAME_FEATURE ) )* ) ;
    public final EObject ruleCOMPONENT_NAME() throws RecognitionException {
        EObject current = null;

        Token lv_name_0_0=null;
        Token otherlv_1=null;
        EObject lv_features_2_0 = null;



        	enterRule();

        try {
            // InternalIDFComponentDsl.g:468:2: ( ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= ':' ( (lv_features_2_0= ruleCOMPONENT_NAME_FEATURE ) )* ) )
            // InternalIDFComponentDsl.g:469:2: ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= ':' ( (lv_features_2_0= ruleCOMPONENT_NAME_FEATURE ) )* )
            {
            // InternalIDFComponentDsl.g:469:2: ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= ':' ( (lv_features_2_0= ruleCOMPONENT_NAME_FEATURE ) )* )
            // InternalIDFComponentDsl.g:470:3: ( (lv_name_0_0= RULE_ID ) ) otherlv_1= ':' ( (lv_features_2_0= ruleCOMPONENT_NAME_FEATURE ) )*
            {
            // InternalIDFComponentDsl.g:470:3: ( (lv_name_0_0= RULE_ID ) )
            // InternalIDFComponentDsl.g:471:4: (lv_name_0_0= RULE_ID )
            {
            // InternalIDFComponentDsl.g:471:4: (lv_name_0_0= RULE_ID )
            // InternalIDFComponentDsl.g:472:5: lv_name_0_0= RULE_ID
            {
            lv_name_0_0=(Token)match(input,RULE_ID,FOLLOW_7); 

            					newLeafNode(lv_name_0_0, grammarAccess.getCOMPONENT_NAMEAccess().getNameIDTerminalRuleCall_0_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getCOMPONENT_NAMERule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_0_0,
            						"org.eclipse.xtext.common.Terminals.ID");
            				

            }


            }

            otherlv_1=(Token)match(input,17,FOLLOW_8); 

            			newLeafNode(otherlv_1, grammarAccess.getCOMPONENT_NAMEAccess().getColonKeyword_1());
            		
            // InternalIDFComponentDsl.g:492:3: ( (lv_features_2_0= ruleCOMPONENT_NAME_FEATURE ) )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==18||LA6_0==20) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // InternalIDFComponentDsl.g:493:4: (lv_features_2_0= ruleCOMPONENT_NAME_FEATURE )
            	    {
            	    // InternalIDFComponentDsl.g:493:4: (lv_features_2_0= ruleCOMPONENT_NAME_FEATURE )
            	    // InternalIDFComponentDsl.g:494:5: lv_features_2_0= ruleCOMPONENT_NAME_FEATURE
            	    {

            	    					newCompositeNode(grammarAccess.getCOMPONENT_NAMEAccess().getFeaturesCOMPONENT_NAME_FEATUREParserRuleCall_2_0());
            	    				
            	    pushFollow(FOLLOW_8);
            	    lv_features_2_0=ruleCOMPONENT_NAME_FEATURE();

            	    state._fsp--;


            	    					if (current==null) {
            	    						current = createModelElementForParent(grammarAccess.getCOMPONENT_NAMERule());
            	    					}
            	    					add(
            	    						current,
            	    						"features",
            	    						lv_features_2_0,
            	    						"com.espressif.idf.componen.IDFComponentDsl.COMPONENT_NAME_FEATURE");
            	    					afterParserOrEnumRuleCall();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCOMPONENT_NAME"


    // $ANTLR start "entryRuleCOMPONENT_NAME_FEATURE"
    // InternalIDFComponentDsl.g:515:1: entryRuleCOMPONENT_NAME_FEATURE returns [EObject current=null] : iv_ruleCOMPONENT_NAME_FEATURE= ruleCOMPONENT_NAME_FEATURE EOF ;
    public final EObject entryRuleCOMPONENT_NAME_FEATURE() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCOMPONENT_NAME_FEATURE = null;


        try {
            // InternalIDFComponentDsl.g:515:63: (iv_ruleCOMPONENT_NAME_FEATURE= ruleCOMPONENT_NAME_FEATURE EOF )
            // InternalIDFComponentDsl.g:516:2: iv_ruleCOMPONENT_NAME_FEATURE= ruleCOMPONENT_NAME_FEATURE EOF
            {
             newCompositeNode(grammarAccess.getCOMPONENT_NAME_FEATURERule()); 
            pushFollow(FOLLOW_1);
            iv_ruleCOMPONENT_NAME_FEATURE=ruleCOMPONENT_NAME_FEATURE();

            state._fsp--;

             current =iv_ruleCOMPONENT_NAME_FEATURE; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCOMPONENT_NAME_FEATURE"


    // $ANTLR start "ruleCOMPONENT_NAME_FEATURE"
    // InternalIDFComponentDsl.g:522:1: ruleCOMPONENT_NAME_FEATURE returns [EObject current=null] : (this_RULES_0= ruleRULES | this_PUBLIC_1= rulePUBLIC ) ;
    public final EObject ruleCOMPONENT_NAME_FEATURE() throws RecognitionException {
        EObject current = null;

        EObject this_RULES_0 = null;

        EObject this_PUBLIC_1 = null;



        	enterRule();

        try {
            // InternalIDFComponentDsl.g:528:2: ( (this_RULES_0= ruleRULES | this_PUBLIC_1= rulePUBLIC ) )
            // InternalIDFComponentDsl.g:529:2: (this_RULES_0= ruleRULES | this_PUBLIC_1= rulePUBLIC )
            {
            // InternalIDFComponentDsl.g:529:2: (this_RULES_0= ruleRULES | this_PUBLIC_1= rulePUBLIC )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==20) ) {
                alt7=1;
            }
            else if ( (LA7_0==18) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // InternalIDFComponentDsl.g:530:3: this_RULES_0= ruleRULES
                    {

                    			newCompositeNode(grammarAccess.getCOMPONENT_NAME_FEATUREAccess().getRULESParserRuleCall_0());
                    		
                    pushFollow(FOLLOW_2);
                    this_RULES_0=ruleRULES();

                    state._fsp--;


                    			current = this_RULES_0;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 2 :
                    // InternalIDFComponentDsl.g:539:3: this_PUBLIC_1= rulePUBLIC
                    {

                    			newCompositeNode(grammarAccess.getCOMPONENT_NAME_FEATUREAccess().getPUBLICParserRuleCall_1());
                    		
                    pushFollow(FOLLOW_2);
                    this_PUBLIC_1=rulePUBLIC();

                    state._fsp--;


                    			current = this_PUBLIC_1;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCOMPONENT_NAME_FEATURE"


    // $ANTLR start "entryRulePUBLIC"
    // InternalIDFComponentDsl.g:551:1: entryRulePUBLIC returns [EObject current=null] : iv_rulePUBLIC= rulePUBLIC EOF ;
    public final EObject entryRulePUBLIC() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePUBLIC = null;


        try {
            // InternalIDFComponentDsl.g:551:47: (iv_rulePUBLIC= rulePUBLIC EOF )
            // InternalIDFComponentDsl.g:552:2: iv_rulePUBLIC= rulePUBLIC EOF
            {
             newCompositeNode(grammarAccess.getPUBLICRule()); 
            pushFollow(FOLLOW_1);
            iv_rulePUBLIC=rulePUBLIC();

            state._fsp--;

             current =iv_rulePUBLIC; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePUBLIC"


    // $ANTLR start "rulePUBLIC"
    // InternalIDFComponentDsl.g:558:1: rulePUBLIC returns [EObject current=null] : (otherlv_0= 'public:' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject rulePUBLIC() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalIDFComponentDsl.g:564:2: ( (otherlv_0= 'public:' ( (lv_name_1_0= RULE_ID ) ) ) )
            // InternalIDFComponentDsl.g:565:2: (otherlv_0= 'public:' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // InternalIDFComponentDsl.g:565:2: (otherlv_0= 'public:' ( (lv_name_1_0= RULE_ID ) ) )
            // InternalIDFComponentDsl.g:566:3: otherlv_0= 'public:' ( (lv_name_1_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,18,FOLLOW_9); 

            			newLeafNode(otherlv_0, grammarAccess.getPUBLICAccess().getPublicKeyword_0());
            		
            // InternalIDFComponentDsl.g:570:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalIDFComponentDsl.g:571:4: (lv_name_1_0= RULE_ID )
            {
            // InternalIDFComponentDsl.g:571:4: (lv_name_1_0= RULE_ID )
            // InternalIDFComponentDsl.g:572:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_2); 

            					newLeafNode(lv_name_1_0, grammarAccess.getPUBLICAccess().getNameIDTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getPUBLICRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.ID");
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePUBLIC"


    // $ANTLR start "entryRuleOVERRIDE_PATH"
    // InternalIDFComponentDsl.g:592:1: entryRuleOVERRIDE_PATH returns [EObject current=null] : iv_ruleOVERRIDE_PATH= ruleOVERRIDE_PATH EOF ;
    public final EObject entryRuleOVERRIDE_PATH() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOVERRIDE_PATH = null;


        try {
            // InternalIDFComponentDsl.g:592:54: (iv_ruleOVERRIDE_PATH= ruleOVERRIDE_PATH EOF )
            // InternalIDFComponentDsl.g:593:2: iv_ruleOVERRIDE_PATH= ruleOVERRIDE_PATH EOF
            {
             newCompositeNode(grammarAccess.getOVERRIDE_PATHRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleOVERRIDE_PATH=ruleOVERRIDE_PATH();

            state._fsp--;

             current =iv_ruleOVERRIDE_PATH; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOVERRIDE_PATH"


    // $ANTLR start "ruleOVERRIDE_PATH"
    // InternalIDFComponentDsl.g:599:1: ruleOVERRIDE_PATH returns [EObject current=null] : (otherlv_0= 'override_path:' ( (lv_name_1_0= RULE_STRING ) ) ) ;
    public final EObject ruleOVERRIDE_PATH() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalIDFComponentDsl.g:605:2: ( (otherlv_0= 'override_path:' ( (lv_name_1_0= RULE_STRING ) ) ) )
            // InternalIDFComponentDsl.g:606:2: (otherlv_0= 'override_path:' ( (lv_name_1_0= RULE_STRING ) ) )
            {
            // InternalIDFComponentDsl.g:606:2: (otherlv_0= 'override_path:' ( (lv_name_1_0= RULE_STRING ) ) )
            // InternalIDFComponentDsl.g:607:3: otherlv_0= 'override_path:' ( (lv_name_1_0= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,19,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getOVERRIDE_PATHAccess().getOverride_pathKeyword_0());
            		
            // InternalIDFComponentDsl.g:611:3: ( (lv_name_1_0= RULE_STRING ) )
            // InternalIDFComponentDsl.g:612:4: (lv_name_1_0= RULE_STRING )
            {
            // InternalIDFComponentDsl.g:612:4: (lv_name_1_0= RULE_STRING )
            // InternalIDFComponentDsl.g:613:5: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            					newLeafNode(lv_name_1_0, grammarAccess.getOVERRIDE_PATHAccess().getNameSTRINGTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getOVERRIDE_PATHRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOVERRIDE_PATH"


    // $ANTLR start "entryRuleRULES"
    // InternalIDFComponentDsl.g:633:1: entryRuleRULES returns [EObject current=null] : iv_ruleRULES= ruleRULES EOF ;
    public final EObject entryRuleRULES() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRULES = null;


        try {
            // InternalIDFComponentDsl.g:633:46: (iv_ruleRULES= ruleRULES EOF )
            // InternalIDFComponentDsl.g:634:2: iv_ruleRULES= ruleRULES EOF
            {
             newCompositeNode(grammarAccess.getRULESRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRULES=ruleRULES();

            state._fsp--;

             current =iv_ruleRULES; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRULES"


    // $ANTLR start "ruleRULES"
    // InternalIDFComponentDsl.g:640:1: ruleRULES returns [EObject current=null] : (otherlv_0= 'rules:' ( (lv_name_1_0= RULE_STRING ) ) ) ;
    public final EObject ruleRULES() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;


        	enterRule();

        try {
            // InternalIDFComponentDsl.g:646:2: ( (otherlv_0= 'rules:' ( (lv_name_1_0= RULE_STRING ) ) ) )
            // InternalIDFComponentDsl.g:647:2: (otherlv_0= 'rules:' ( (lv_name_1_0= RULE_STRING ) ) )
            {
            // InternalIDFComponentDsl.g:647:2: (otherlv_0= 'rules:' ( (lv_name_1_0= RULE_STRING ) ) )
            // InternalIDFComponentDsl.g:648:3: otherlv_0= 'rules:' ( (lv_name_1_0= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,20,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getRULESAccess().getRulesKeyword_0());
            		
            // InternalIDFComponentDsl.g:652:3: ( (lv_name_1_0= RULE_STRING ) )
            // InternalIDFComponentDsl.g:653:4: (lv_name_1_0= RULE_STRING )
            {
            // InternalIDFComponentDsl.g:653:4: (lv_name_1_0= RULE_STRING )
            // InternalIDFComponentDsl.g:654:5: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            					newLeafNode(lv_name_1_0, grammarAccess.getRULESAccess().getNameSTRINGTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRULESRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRULES"

    // Delegated rules


 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x000000000000F802L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000090022L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0000000000140002L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000000020L});

}