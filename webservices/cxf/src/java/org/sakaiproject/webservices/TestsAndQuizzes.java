package org.sakaiproject.webservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.sakaiproject.tool.assessment.samlite.api.QuestionGroup;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.util.FormattedText;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Expose Test and Quizzes via web services
 */

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class TestsAndQuizzes extends AbstractWebService {	

	private static final Log LOG = LogFactory.getLog(TestsAndQuizzes.class);

	/** 
	 * createAsessmentFromText - WS Endpoint, exposing the SamLite createImportedAssessment()
	 *
	 * @param	String sessionid		the id of a valid admin session
	 * @param	String siteid			the enterprise/sakai id of the site to be archived
	 * @param	String siteproperty		the property that holds the enterprise site id
	 * @param	String title			the title of the assessment
	 * @param	String description		the description of the assessment
	 * @param	String template			the assessment template name to use when importing the assessment
	 *						*note templates must be created with admin user
	 * @param	String textdata			the question data in the word2qti format
	 * @return	boolean	       		 	returns true if assessment created successfully, false if assessment is null
	 * 
	 * @throws	AxisFault			WS TestsAndQuizzes.createAssessmentFromText(): SamLiteService.parse() returned a null QuestionGroup
	 *						WS TestsAndQuizzes.createAssessmentFromText(): SamLiteService.createDocument() returned a null QTI Document
	 *
	 */
    @WebMethod
    @Path("/createAssessmentFromText")
    @Produces("text/plain")
    @GET
    public boolean createAssessmentFromText(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "siteproperty", partName = "siteproperty") @QueryParam("siteproperty") String siteproperty,
            @WebParam(name = "title", partName = "title") @QueryParam("title") String title,
            @WebParam(name = "description", partName = "description") @QueryParam("description") String description,
            @WebParam(name = "template", partName = "template") @QueryParam("template") String template,
            @WebParam(name = "textdata", partName = "textdata") @QueryParam("textdata") String textdata) {
        Session session = establishSession(sessionid);
		Document document = null;

		QuestionGroup questionGroup = samLiteService.parse(FormattedText.escapeHtml(title, false), FormattedText.escapeHtml(description, false), FormattedText.escapeHtml(textdata, false));
		if (questionGroup == null) {
			throw new RuntimeException("WS TestsAndQuizzes.createAssessmentFromText(): SamLiteService.parse() returned a null QuestionGroup");
		}

		document = samLiteService.createDocument(questionGroup);
		if (document == null) {
			throw new RuntimeException("WS TestsAndQuizzes.createAssessmentFromText(): SamLiteService.createDocument() returned a null QTI Document");
		}

		return createAssessment(siteid, siteproperty, title, description, template, document);
	}

	/** 
	 * createAsessmentFromExport - WS Endpoint, exposing the SamLite createImportedAssessment()
	 *
	 * @param	String sessionid		the id of a valid admin session
	 * @param	String siteid			the enterprise/sakai id of the site to be archived
	 * @param	String siteproperty		the property that holds the enterprise site id
	 * @param	String xmlstring		the IMS QTI document containing the assessment
	 * @return	boolean	       		 	returns true if assessment created successfully, false if assessment is null
	 * 
	 * @throws	AxisFault			WS TestsAndQuizzes.createAssessmentFromXml(): returned a null QTI Document
	 *						WS TestsAndQuizzes.createAssessmentFromXml(): " + e.getMessage
	 *
	 */
    @WebMethod
    @Path("/createAssessmentFromExport")
    @Produces("text/plain")
    @GET
    public boolean createAssessmentFromExport(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "siteproperty", partName = "siteproperty") @QueryParam("siteproperty") String siteproperty,
            @WebParam(name = "xmlstring", partName = "xmlstring") @QueryParam("xmlstring") String xmlstring) {
        Session session = establishSession(sessionid);
		Document document = null;
		InputStream inputStream = null;

		try {
			byte[] bytes = xmlstring.getBytes();

			inputStream = new ByteArrayInputStream(bytes);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			builderFactory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
			document = documentBuilder.parse(inputStream);
		} catch(Exception e) {
			LOG.error("WS TestsAndQuizzes.createAssessmentFromXml(): " + e.getMessage(), e);
			throw new RuntimeException("WS TestsAndQuizzes.createAssessmentFromXml(): " + e.getMessage());
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
			}
		}

		if (document == null) {
			throw new RuntimeException("WS TestsAndQuizzes.createAssessmentFromXml(): returned a null QTI Document");
		}
		
		return createAssessment(siteid, siteproperty, null, null, null, document);
	}

	/** 
	 * createAsessmentFromExportFile - WS Endpoint, exposing the SamLite createImportedAssessment()
	 *
	 * @param	String sessionid		the id of a valid admin session
	 * @param	String siteid			the enterprise/sakai id of the site to be archived
	 * @param	String siteproperty		the property that holds the enterprise site id
	 * @param	String xmlfile			path to the IMS QTI document containing the assessment
	 * @return	boolean	       		 	returns true if assessment created successfully, false if assessment is null
	 * 
	 * @throws	AxisFault			WS TestsAndQuizzes.createAssessmentFromXml(): XmlUtil.createDocument() returned a null QTI Document
	 * 						WS TestsAndQuizzes.createAssessmentFromXml(): XmlUtil.createDocument() ParserConfigurationException: 
	 *						WS TestsAndQuizzes.createAssessmentFromXml(): XmlUtil.createDocument() SaxException:
	 *						WS TestsAndQuizzes.createAssessmentFromXml(): XmlUtil.createDocument() IOException: 
	 *
	 */

    @WebMethod
    @Path("/createAssessmentFromExportFile")
    @Produces("text/plain")
    @GET
    public boolean createAssessmentFromExportFile(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteid", partName = "siteid") @QueryParam("siteid") String siteid,
            @WebParam(name = "siteproperty", partName = "siteproperty") @QueryParam("siteproperty") String siteproperty,
            @WebParam(name = "xmlfile", partName = "xmlfile") @QueryParam("xmlfile") String xmlfile) {
        Session session = establishSession(sessionid);
		Document document = null;

		try {
			document = XmlUtil.readDocument(xmlfile, true);
		} catch (ParserConfigurationException pce) {
			LOG.error("WS TestsAndQuizzes.createAssessmentFromExportFile(): XmlUtil.createDocument() ParserConfigurationException: " + pce.getMessage(), pce);
			throw new RuntimeException("WS TestsAndQuizzes.createAssessmentFromExportFile(): XmlUtil.createDocument() ParserConfigurationException: " + pce.getMessage());
		} catch (SAXException saxe) {
			LOG.error("WS TestsAndQuizzes.createAssessmentFromExportFile(): XmlUtil.createDocument() SaxException: " + saxe.getMessage(), saxe);
			throw new RuntimeException("WS TestsAndQuizzes.createAssessmentFromExportFile(): XmlUtil.createDocument() SaxException: " + saxe.getMessage());
		} catch (IOException ioe) {
			LOG.error("WS TestsAndQuizzes.createAssessmentFromExportFile(): XmlUtil.createDocument() IOException: " + ioe.getMessage(), ioe);
			throw new RuntimeException("WS TestsAndQuizzes.createAssessmentFromExportFile(): XmlUtil.createDocument() IOException: " + ioe.getMessage());
		}
		if (document == null) {
			throw new RuntimeException("WS TestsAndQuizzes.createAssessmentFromExportFile(): XmlUtil.createDocument() returned a null QTI Document");
		}
		
		return createAssessment(siteid, siteproperty, null, null, null, document);
	}

	/** 
	 * createAsessment - WS Endpoint, exposing the SamLite createImportedAssessment()
	 *
	 * @param	String siteid			the enterprise/sakai id of the site to be archived
	 * @param	String siteproperty		the property that holds the enterprise site id
	 * @param	String title			the title of the assessment
	 * @param	String description		the description of the assessment
	 * @param	String template			the assessment template name to use when importing the assessment
	 *						*note templates must be created with admin user
	 * @param	Document document		the assessment document to import
	 * @return	boolean	       		 	returns true if assessment created successfully, false if assessment is null
	 * 
	 * @throws	AxisFault			WS TestsAndQuizzes.createAssessment(): Site not found - "+ siteid
	 *						WS TestsAndQuizzes.createAssessment(): Could not find template with name - " + template
	 *
	 */
	
	private boolean createAssessment(String siteid, String siteproperty, String title, String description, String template, Document document) {
	
		Site site = null;
		String templateId = null;

		if (siteproperty != null && siteproperty.length() > 0) {
			// find sakai site id using enterprise siteproperty=siteid
			site = findSite(siteproperty, siteid);
		} else {
			try {
				// use siteid as the sakai site id
				site = siteService.getSite(siteid);
			} catch (IdUnusedException ieu) {
				site = null;
			}
		}

		if (site == null) {
			LOG.warn("WS TestsAndQuizzes.createAssessment(): Site not found - " + siteid);
			throw new RuntimeException("WS TestsAndQuizzes.createAssessment(): Site not found - "+ siteid);
		}

		if (template != null && template.length() > 0) {
			// try and find a matching template
			templateId  = findAssessmentTemplateId(template);
			if (templateId == null) {
				throw new RuntimeException("WS TestsAndQuizzes.createAssessment(): Could not find template with name - " + template);
			}
		} else {
			templateId = AssessmentTemplateFacade.DEFAULTTEMPLATE.toString();
		}

		LOG.debug("WS TestsAndQuizzes.createAssessment(): creating assessment - " + title + " in site " + site.getId());
		
		QTIService qtiService = new QTIService();
		AssessmentFacade assessment = qtiService.createImportedAssessment(document, QTIVersion.VERSION_1_2, null, templateId, site.getId());

		if (assessment == null) {
			return false;
		}
		return true;
	}

	/** 
	 * findSite - find the sakai site that contains a unique propertyName=propertyValue
	 *
	 * @param	String propertyName		the sakai site property holding the enterprise site id
	 * @param	String propertyValue		the enterprise site id
	 * @return	Site		        	Site object or null if no site
	 * 
	 * @throws	AxisFault			WS TestsAndQuizzes.findSite(): Too many sites found with property - propertyName=propertyValue
	 *
	 */

	private Site findSite(String propertyName, String propertyValue) {
		LOG.debug("WS TestsAndQuizzes.findSite(): propertyName - " + propertyName + ", propertyValue - " + propertyValue);
		Map propertyCriteria = new HashMap();

		// Replace search property
		propertyCriteria.put(propertyName, propertyValue);

		List list = siteService.getSites(SelectionType.ANY, null, null, propertyCriteria, SortType.NONE, null);

		if (list != null) {
			if (list.size() == 1) {
				return (Site) list.get(0);
			} else if (list.size() > 1) {
				LOG.warn("WS TestsAndQuizzes.findSite(): Too many sites found with property - " + propertyName + "=" + propertyValue);
				throw new RuntimeException("WS TestsAndQuizzes.findSite(): Too many sites found with property - " + propertyName + "=" + propertyValue);
			}

        	}
		return null;
	}

	/** 
	 * findAssessmentTemplateId - find the assessment template id
	 *
	 * @param	String title			the title of the template to look for
	 * @return	String		        	the template id if one was found otherwise null
	 * 
	 */

	private String findAssessmentTemplateId(String title) {
		LOG.debug("WS TestsAndQuizzes.findAssessmentTemplateId(): template title - " + title);
		
		AssessmentService aService = new AssessmentService();

		if (aService != null) {
			// will only look at templates created by the authenticated user
			List templates = aService.getTitleOfAllActiveAssessmentTemplates();
		
			for (int i = 0; i < templates.size(); i++) {
				AssessmentTemplateFacade facade = (AssessmentTemplateFacade) templates.get(i);
				if (facade.getTitle().equals(title)) {
					return facade.getAssessmentBaseId().toString();
				}
			}
		}
		return null;
	}
	
}
