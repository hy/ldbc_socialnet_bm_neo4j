package com.ldbc.socialnet.neo4j.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;

import com.ldbc.socialnet.neo4j.CsvFileInserter;
import com.ldbc.socialnet.neo4j.CsvLineInserter;
import com.ldbc.socialnet.neo4j.domain.CommentsBatchIndex;
import com.ldbc.socialnet.neo4j.domain.Domain;
import com.ldbc.socialnet.neo4j.domain.EmailAddressesBatchIndex;
import com.ldbc.socialnet.neo4j.domain.ForumsBatchIndex;
import com.ldbc.socialnet.neo4j.domain.LanguagesBatchIndex;
import com.ldbc.socialnet.neo4j.domain.LocationsBatchIndex;
import com.ldbc.socialnet.neo4j.domain.OrganisationsBatchIndex;
import com.ldbc.socialnet.neo4j.domain.PersonsBatchIndex;
import com.ldbc.socialnet.neo4j.domain.PostsBatchIndex;
import com.ldbc.socialnet.neo4j.domain.TagClassesBatchIndex;
import com.ldbc.socialnet.neo4j.domain.TagsBatchIndex;

public class CsvFileInserters
{
    private final static Map<String, Object> EMPTY_MAP = new HashMap<String, Object>();
    private final static String RAW_DATA_DIR = "/home/alex/workspace/java/ldbc_socialnet_bm/ldbc_socialnet_dbgen/outputDir/";
    private final static Logger logger = Logger.getLogger( CsvFileInserters.class );

    public static List<CsvFileInserter> all( BatchInserter batchInserter, BatchInserterIndexProvider batchIndexProvider )
            throws FileNotFoundException
    {
        /*
        * Neo4j Batch Index Providers
        */
        CommentsBatchIndex commentsIndex = new CommentsBatchIndex( batchIndexProvider );
        PostsBatchIndex postsIndex = new PostsBatchIndex( batchIndexProvider );
        PersonsBatchIndex personsIndex = new PersonsBatchIndex( batchIndexProvider );
        ForumsBatchIndex forumsIndex = new ForumsBatchIndex( batchIndexProvider );
        TagsBatchIndex tagsIndex = new TagsBatchIndex( batchIndexProvider );
        TagClassesBatchIndex tagClassesIndex = new TagClassesBatchIndex( batchIndexProvider );
        OrganisationsBatchIndex organisationsIndex = new OrganisationsBatchIndex( batchIndexProvider );
        LanguagesBatchIndex languagesIndex = new LanguagesBatchIndex( batchIndexProvider );
        LocationsBatchIndex locationsIndex = new LocationsBatchIndex( batchIndexProvider );
        EmailAddressesBatchIndex emailAddressesIndex = new EmailAddressesBatchIndex( batchIndexProvider );

        /*
        * CSV Files
        */
        List<CsvFileInserter> fileInserters = new ArrayList<CsvFileInserter>();
        fileInserters.add( comments( batchInserter, commentsIndex ) );
        fileInserters.add( posts( batchInserter, postsIndex ) );
        fileInserters.add( persons( batchInserter, personsIndex ) );
        fileInserters.add( forums( batchInserter, forumsIndex ) );
        fileInserters.add( tags( batchInserter, tagsIndex ) );
        fileInserters.add( tagClasses( batchInserter, tagClassesIndex ) );
        fileInserters.add( organisations( batchInserter, organisationsIndex ) );
        fileInserters.add( locations( batchInserter, locationsIndex ) );
        fileInserters.add( commentReplyOfComment( batchInserter, commentsIndex ) );
        fileInserters.add( commentReplyOfPost( batchInserter, commentsIndex, postsIndex ) );
        fileInserters.add( commentIsLocatedInLocation( batchInserter, commentsIndex, locationsIndex ) );
        fileInserters.add( locationPartOfLocation( batchInserter, locationsIndex ) );
        fileInserters.add( personKnowsPerson( batchInserter, personsIndex ) );
        fileInserters.add( personStudyAtOrganisation( batchInserter, personsIndex, organisationsIndex ) );
        fileInserters.add( personSpeaksLanguage( batchInserter, personsIndex, languagesIndex ) );
        fileInserters.add( commentHasCreatorPerson( batchInserter, personsIndex, commentsIndex ) );
        fileInserters.add( postHasCreatorPerson( batchInserter, personsIndex, postsIndex ) );
        fileInserters.add( forumHasModeratorPerson( batchInserter, personsIndex, forumsIndex ) );
        fileInserters.add( personIsLocatedInLocation( batchInserter, personsIndex, locationsIndex ) );
        fileInserters.add( personWorksAtOrganisation( batchInserter, personsIndex, organisationsIndex ) );
        fileInserters.add( personHasInterestTag( batchInserter, personsIndex, tagsIndex ) );
        fileInserters.add( personHasEmailAddress( batchInserter, personsIndex, emailAddressesIndex ) );
        fileInserters.add( postHasTagTag( batchInserter, postsIndex, tagsIndex ) );
        fileInserters.add( personLikesPost( batchInserter, personsIndex, postsIndex ) );
        fileInserters.add( postIsLocatedInLocation( batchInserter, postsIndex, locationsIndex ) );
        fileInserters.add( forumHasMemberPerson( batchInserter, forumsIndex, personsIndex ) );
        fileInserters.add( forumContainerOfPost( batchInserter, forumsIndex, postsIndex ) );
        fileInserters.add( forumHasTag( batchInserter, forumsIndex, tagsIndex ) );
        fileInserters.add( tagHasTypeTagClass( batchInserter, tagsIndex, tagClassesIndex ) );
        fileInserters.add( tagClassIsSubclassOfTagClass( batchInserter, tagClassesIndex ) );
        fileInserters.add( organisationBasedNearLocation( batchInserter, organisationsIndex, locationsIndex ) );

        return fileInserters;
    }

    private static CsvFileInserter comments( final BatchInserter batchInserter, final CommentsBatchIndex commentsIndex )
            throws FileNotFoundException
    {
        /*
        id  creationDate            location IP     browserUsed     content
        00  2010-03-11T10:11:18Z    14.134.0.11     Chrome          About Michael Jordan, Association...
         */

        return new CsvFileInserter( new File( RAW_DATA_DIR + "comment.csv" ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                int id = Integer.parseInt( (String) columnValues[0] );
                properties.put( "id", id );
                // TODO convert to datetime
                // 2010-12-28T07:16:25Z
                properties.put( "creationDate", columnValues[1] );
                properties.put( "locationIP", columnValues[2] );
                properties.put( "browserUsed", columnValues[3] );
                properties.put( "content", columnValues[4] );
                long commentNodeId = batchInserter.createNode( properties, Domain.Node.COMMENT );
                commentsIndex.getIndex().add( commentNodeId, MapUtil.map( "id", id ) );
            }
        } );
    }

    private static CsvFileInserter posts( final BatchInserter batchInserter, final PostsBatchIndex postsIndex )
            throws FileNotFoundException
    {
        /*
        id      imageFile   creationDate            locationIP      browserUsed     language    content
        100     photo9.jpg  2010-03-11T05:28:04Z    27.99.128.8     Firefox         zh          About Michael Jordan...
        */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "post.csv" ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                int id = Integer.parseInt( (String) columnValues[0] );
                properties.put( "id", id );
                properties.put( "imageFile", columnValues[1] );
                // TODO dateTime
                properties.put( "creationDate", columnValues[2] );
                properties.put( "locationIP", columnValues[3] );
                properties.put( "browserUsed", columnValues[4] );
                properties.put( "language", columnValues[5] );
                properties.put( "content", columnValues[6] );
                long postNodeId = batchInserter.createNode( properties, Domain.Node.POST );
                postsIndex.getIndex().add( postNodeId, MapUtil.map( "id", id ) );
            }
        } );
    }

    private static CsvFileInserter persons( final BatchInserter batchInserter, final PersonsBatchIndex personsIndex )
            throws FileNotFoundException
    {
        /*
        id      firstName   lastName    gender  birthday    creationDate            locationIP      browserUsed
        75      Fernanda    Alves       male    1984-12-15  2010-12-14T11:41:37Z    143.106.0.7     Firefox
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "person.csv" ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                int id = Integer.parseInt( (String) columnValues[0] );
                properties.put( "id", id );
                properties.put( "firstName", columnValues[1] );
                properties.put( "lastName", columnValues[2] );
                properties.put( "gender", columnValues[3] );
                // TODO date
                properties.put( "birthday", columnValues[4] );
                // TODO datetime
                properties.put( "creationDate", columnValues[5] );
                properties.put( "locationIP", columnValues[6] );
                properties.put( "browserUsed", columnValues[7] );
                long personNodeId = batchInserter.createNode( properties, Domain.Node.PERSON );
                personsIndex.getIndex().add( personNodeId, MapUtil.map( "id", id ) );
            }
        } );
    }

    private static CsvFileInserter forums( final BatchInserter batchInserter, final ForumsBatchIndex forumIndex )
            throws FileNotFoundException
    {
        /*
            id      title                       creationDate
            150     Wall of Fernanda Alves      2010-12-14T11:41:37Z
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "forum.csv" ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                int id = Integer.parseInt( (String) columnValues[0] );
                properties.put( "id", id );
                properties.put( "title", columnValues[1] );
                // TODO datetime
                properties.put( "creationDate", columnValues[2] );
                long forumNodeId = batchInserter.createNode( properties, Domain.Node.FORUM );
                forumIndex.getIndex().add( forumNodeId, MapUtil.map( "id", id ) );
            }
        } );
    }

    private static CsvFileInserter tags( final BatchInserter batchInserter, final TagsBatchIndex tagIndex )
            throws FileNotFoundException
    {
        /*
        id      name                url
        259     Gilberto_Gil        http://dbpedia.org/resource/Gilberto_Gil
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "tag.csv" ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                int id = Integer.parseInt( (String) columnValues[0] );
                properties.put( "id", id );
                properties.put( "name", columnValues[1] );
                properties.put( "url", columnValues[2] );
                long tagNodeId = batchInserter.createNode( properties, Domain.Node.TAG );
                tagIndex.getIndex().add( tagNodeId, MapUtil.map( "id", id ) );
            }
        } );
    }

    private static CsvFileInserter tagClasses( final BatchInserter batchInserter,
            final TagClassesBatchIndex tagClassesIndex ) throws FileNotFoundException
    {
        /*
        id      name    url
        211     Person  http://dbpedia.org/ontology/Person
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "tagclass.csv" ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                int id = Integer.parseInt( (String) columnValues[0] );
                properties.put( "id", id );
                properties.put( "name", columnValues[1] );
                properties.put( "url", columnValues[2] );
                long tagClassNodeId = batchInserter.createNode( properties, Domain.Node.TAG_CLASS );
                tagClassesIndex.getIndex().add( tagClassNodeId, MapUtil.map( "id", id ) );
            }
        } );
    }

    private static CsvFileInserter organisations( final BatchInserter batchInserter,
            final OrganisationsBatchIndex organisationsIndex ) throws FileNotFoundException
    {
        /*
        id  type        name                        url
        00  university  Universidade_de_Pernambuco  http://dbpedia.org/resource/Universidade_de_Pernambuco
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "organisation.csv" ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                int id = Integer.parseInt( (String) columnValues[0] );
                properties.put( "id", id );
                properties.put( "name", columnValues[2] );
                // only necessary if connecting to dbpedia
                // properties.put( "url", columnValues[3] );
                long organisationNodeId = batchInserter.createNode( properties, Domain.Node.ORGANISATION,
                        Domain.OrganisationType.valueOf( ( (String) columnValues[1] ).toUpperCase() ) );
                organisationsIndex.getIndex().add( organisationNodeId, MapUtil.map( "id", id ) );
            }
        } );
    }

    private static CsvFileInserter locations( final BatchInserter batchInserter, final LocationsBatchIndex locationIndex )
            throws FileNotFoundException
    {
        /*
        id      name            url                                             type
        5170    South_America   http://dbpedia.org/resource/South_America       REGION
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "location.csv" ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                int id = Integer.parseInt( (String) columnValues[0] );
                properties.put( "id", id );
                properties.put( "name", columnValues[1] );
                properties.put( "url", columnValues[2] );
                // LocationType = COUNTRY | CITY | REGION
                long locationNodeId = batchInserter.createNode( properties, Domain.Node.LOCATION,
                        Domain.LocationType.valueOf( ( (String) columnValues[3] ).toUpperCase() ) );
                locationIndex.getIndex().add( locationNodeId, MapUtil.map( "id", id ) );
            }
        } );
    }

    private static CsvFileInserter commentReplyOfComment( final BatchInserter batchInserter,
            final CommentsBatchIndex commentsIndex ) throws FileNotFoundException
    {
        /*
        Comment.id  Comment.id
        20          00
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "comment_replyOf_comment.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromCommentNodeId = commentsIndex.getIndex().get( "id",
                        Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long toCommentNodeId = commentsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { fromCommentNodeId, toCommentNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.REPLY_OF,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter commentReplyOfPost( final BatchInserter batchInserter,
            final CommentsBatchIndex commentsIndex, final PostsBatchIndex postsIndex ) throws FileNotFoundException
    {
        /*
        Comment.id  Post.id
        00          100
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "comment_replyOf_post.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromCommentNodeId = commentsIndex.getIndex().get( "id",
                        Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long toPostNodeId = postsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { fromCommentNodeId, toPostNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.REPLY_OF,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter commentIsLocatedInLocation( final BatchInserter batchInserter,
            final CommentsBatchIndex commentsIndex, final LocationsBatchIndex locationsIndex )
            throws FileNotFoundException
    {
        /*
        Comment.id  Location.id
        100         73
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "comment_isLocatedIn_location.csv" ),
                new CsvLineInserter()
                {
                    @Override
                    public Object[] transform( Object[] columnValues )
                    {
                        long fromCommentNodeId = commentsIndex.getIndex().get( "id",
                                Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                        long toLocationNodeId = locationsIndex.getIndex().get( "id",
                                Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                        return new Object[] { fromCommentNodeId, toLocationNodeId };
                    }

                    @Override
                    public void insert( Object[] columnValues )
                    {
                        batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                                Domain.Rel.IS_LOCATED_IN, EMPTY_MAP );
                    }
                } );
    }

    private static CsvFileInserter locationPartOfLocation( final BatchInserter batchInserter,
            final LocationsBatchIndex locationsIndex ) throws FileNotFoundException
    {
        /*
        Location.id Location.id
        11          5170
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "location_partOf_location.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromLocationNodeId = locationsIndex.getIndex().get( "id",
                        Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long toLocationNodeId = locationsIndex.getIndex().get( "id",
                        Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { fromLocationNodeId, toLocationNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.IS_PART_OF, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personKnowsPerson( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex ) throws FileNotFoundException
    {
        /*
        Person.id   Person.id
        75          1489
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "person_knows_person.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromPersonNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long toPersonNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { fromPersonNodeId, toPersonNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.KNOWS,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personStudyAtOrganisation( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final OrganisationsBatchIndex organisationsIndex )
            throws FileNotFoundException
    {
        /*
        Person.id   Organisation.id classYear
        75          00                  2004
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "person_studyAt_organisation.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromPersonNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long toOrganisationNodeId = organisationsIndex.getIndex().get( "id",
                        Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                int classYear = Integer.parseInt( (String) columnValues[2] );
                return new Object[] { fromPersonNodeId, toOrganisationNodeId, classYear };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put( "classYear", columnValues[2] );
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.STUDY_AT,
                        properties );
            }
        } );
    }

    private static CsvFileInserter personSpeaksLanguage( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final LanguagesBatchIndex languagesIndex )
            throws FileNotFoundException
    {
        /*        
        Person.id   language
        75          pt
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "person_speaks_language.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long personNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                return new Object[] { personNodeId, columnValues[1] };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.setNodeProperty( (Long) columnValues[0], "language", columnValues[1] );
            }
        } );
    }

    private static CsvFileInserter commentHasCreatorPerson( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final CommentsBatchIndex commentsIndex ) throws FileNotFoundException
    {
        /*        
        Comment.id  Person.id
        00          1402
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "comment_hasCreator_person.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long commentNodeId = commentsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long personNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { commentNodeId, personNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.HAS_CREATOR, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter postHasCreatorPerson( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final PostsBatchIndex postsIndex ) throws FileNotFoundException
    {
        /*
        Post.id     Person.id
        00          75
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "post_hasCreator_person.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long postNodeId = postsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long personNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { postNodeId, personNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.HAS_CREATOR, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter forumHasModeratorPerson( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final ForumsBatchIndex forumsIndex ) throws FileNotFoundException
    {
        /*
        Forum.id    Person.id
        1500        75
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "forum_hasModerator_person.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long forumNodeId = 0;
                try
                {
                    forumNodeId = forumsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                }
                catch ( Exception e )
                {
                    /*
                     * TODO remove exception handling after data generator is fixed
                     * usually ids in colummn 0 of forum.csv (and other .csv files) have 0 suffix
                     * in forum.csv some rows do not, for example:
                     *    2978|Wall of Lei Liu|2010-03-11T03:55:32Z
                     * then files like person_moderator_of_forum.csv attempt to retrieve 29780
                     */
                    logger.error( "Forum node not found: " + columnValues[0] );
                    return null;
                }
                long personNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { forumNodeId, personNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                // TODO remove after data generator fixed
                if ( columnValues == null ) return;

                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.HAS_MODERATOR, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personIsLocatedInLocation( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final LocationsBatchIndex locationsIndex )
            throws FileNotFoundException
    {
        /*        
        Person.id   Location.id
        75          310
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "person_isLocatedIn_location.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long personNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long locationNodeId = locationsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { personNodeId, locationNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.IS_LOCATED_IN, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personWorksAtOrganisation( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final OrganisationsBatchIndex organisationsIndex )
            throws FileNotFoundException
    {
        /*
        Person.id   Organisation.id     workFrom
        75          10                  2016
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "person_workAt_organisation.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long personNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long organisationNodeId = organisationsIndex.getIndex().get( "id",
                        Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                int workFrom = Integer.parseInt( (String) columnValues[2] );
                return new Object[] { personNodeId, organisationNodeId, workFrom };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put( "workFrom", columnValues[2] );
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.WORKS_AT,
                        properties );
            }
        } );
    }

    private static CsvFileInserter personHasInterestTag( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final TagsBatchIndex tagsIndex ) throws FileNotFoundException
    {
        /*
        Person.id   Tag.id
        75          259
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "person_hasInterest_tag.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long personNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long tagNodeId = tagsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { personNodeId, tagNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.HAS_INTEREST, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personHasEmailAddress( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final EmailAddressesBatchIndex emailAddressesIndex )
            throws FileNotFoundException
    {
        /*
        Person.id   email
        75          Fernanda75@gmx.com
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "person_hasEmail_emailaddress.csv" ),
                new CsvLineInserter()
                {
                    @Override
                    public void insert( Object[] columnValues )
                    {
                        long personNodeId = personsIndex.getIndex().get( "id",
                                Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                        batchInserter.setNodeProperty( personNodeId, "email", columnValues[1] );
                    }
                } );
    }

    private static CsvFileInserter postHasTagTag( final BatchInserter batchInserter, final PostsBatchIndex postsIndex,
            final TagsBatchIndex tagsIndex ) throws FileNotFoundException
    {
        /*
        Post.id Tag.id
        100     2903
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "post_hasTag_tag.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long postNodeId = postsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long tagNodeId = tagsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { postNodeId, tagNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                // TODO should Tag be a Label too?
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.HAS_TAG,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter postAnnotatedWithLanguage( final BatchInserter batchInserter,
            final PostsBatchIndex postsIndex, final LanguagesBatchIndex languagesIndex ) throws FileNotFoundException
    {
        /*
        TODO "annotatedWith" relationship not in schema table

        id  Post.id     Language.id
        00  75          259
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "post_annotated_language.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                int id = Integer.parseInt( (String) columnValues[0] );
                long fromPostNodeId = postsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                long toLanguageNodeId = 0;
                try
                {
                    toLanguageNodeId = languagesIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[2] ) ).getSingle();
                }
                catch ( Exception e )
                {
                    /*
                     * TODO remove exception handling after data generator is fixed
                     * at present sometimes it occurs that languageId == -1
                     */
                    return null;
                }
                return new Object[] { id, fromPostNodeId, toLanguageNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                // TODO remove when data generator fixed
                if ( columnValues == null ) return;

                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put( "id", columnValues[0] );
                batchInserter.createRelationship( (Long) columnValues[1], (Long) columnValues[2],
                        Domain.Rel.ANNOTATED_WITH, properties );
            }
        } );
    }

    private static CsvFileInserter personLikesPost( final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final PostsBatchIndex postsIndex ) throws FileNotFoundException
    {
        /*
        Person.id   Post.id     creationDate
        1489        00          2011-01-20T11:18:41Z
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "person_likes_post.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromPersonNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long toPostNodeId = postsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                // TODO dateTime
                Object creationDate = columnValues[2];
                return new Object[] { fromPersonNodeId, toPostNodeId, creationDate };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put( "creationDate", columnValues[2] );
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.LIKES,
                        properties );
            }
        } );
    }

    private static CsvFileInserter postIsLocatedInLocation( final BatchInserter batchInserter,
            final PostsBatchIndex postsIndex, final LocationsBatchIndex locationsIndex ) throws FileNotFoundException
    {
        /*
        Post.id     Location.id
        00          11
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "post_isLocatedIn_location.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long postNodeId = postsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long locationNodeId = locationsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { postNodeId, locationNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.IS_LOCATED_IN, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter forumHasMemberPerson( final BatchInserter batchInserter,
            final ForumsBatchIndex forumsIndex, final PersonsBatchIndex personsIndex ) throws FileNotFoundException
    {
        /*
        Forum.id    Person.id   joinDate
        150         1489        2011-01-02T01:01:10Z        
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "forum_hasMember_person.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long forumNodeId = forumsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long personNodeId = personsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();

                if ( columnValues.length == 3 )
                {
                    // TODO dateTime
                    Object joinDate = columnValues[2];
                    return new Object[] { forumNodeId, personNodeId, joinDate };
                }
                else
                {
                    logger.error( "Line only has 2 columns" );
                    return new Object[] { forumNodeId, personNodeId };
                }
            }

            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                if ( columnValues.length == 3 )
                {
                    properties.put( "joinDate", columnValues[2] );
                }
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.HAS_MEMBER, properties );
            }
        } );
    }

    private static CsvFileInserter forumContainerOfPost( final BatchInserter batchInserter,
            final ForumsBatchIndex forumsIndex, final PostsBatchIndex postsIndex ) throws FileNotFoundException
    {
        /*
        Forum.id    Post.id
        40220       00
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "forum_container_of_post.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long forumNodeId = 0;
                try
                {
                    forumNodeId = forumsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                }
                catch ( Exception e )
                {
                    /*
                     * TODO remove exception handling after data generator is fixed
                     * usually ids in colummn 1 of forum_container_of_post.csv (and other .csv files) have 0 suffix
                     * in forum_container_of_post.csv some rows do not, for example:
                     *    50294
                     * then when trying to retrieve 50294 (probably supposed to be 502940) from forum.csv it is not found
                     */
                    logger.error( "Forum not found: " + columnValues[0] );
                    return null;
                }
                long postNodeId = postsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { forumNodeId, postNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                // TODO remove after data generator fixed
                if ( columnValues == null ) return;
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.CONTAINER_OF, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter forumHasTag( final BatchInserter batchInserter, final ForumsBatchIndex forumsIndex,
            final TagsBatchIndex tagsIndex ) throws FileNotFoundException
    {
        /*
        Forum.id    Tag.id
        75          259
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "forum_hasTag_tag.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long forumNodeId = 0;
                try
                {
                    forumNodeId = forumsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                }
                catch ( Exception e )
                {
                    /*
                     * TODO remove exception handling when generator is fixed
                     * almost ALL entries in forum_hastag_tag.csv column 2, Forum.id, contain id values that are not in forum.csv
                     * they have no 0 suffix either e.g. 6358
                     * 
                     * of 30346 entries only 1028 appear to be valid
                     */
                    logger.error( "Forum not found: " + columnValues[0] );
                    return null;
                }
                long tagNodeId = 0;
                try
                {
                    tagNodeId = tagsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                }
                catch ( Exception e )
                {
                    /*
                     * TODO remove exception handling when generator fixed
                     * currently forum_hastag_tag.csv contains Tag.id entries in column 1 that are not in tag.csv
                     * for example: 75 in forum_hastag_tag.csv but the closest to that number in tag.csv is 74
                     */
                    logger.error( "Tag not found: " + columnValues[1] );
                    return null;
                }
                return new Object[] { tagNodeId, forumNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                // TODO remove after data generator fixed
                if ( columnValues == null ) return;
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.HAS_TAG,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter tagHasTypeTagClass( final BatchInserter batchInserter,
            final TagsBatchIndex tagsIndex, final TagClassesBatchIndex tagClassesIndex ) throws FileNotFoundException
    {
        /*
        Tag.id  TagClass.id
        259     211
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "tag_hasType_tagclass.csv" ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long tagNodeId = tagsIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                long tagClassNodeId = tagClassesIndex.getIndex().get( "id", Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                return new Object[] { tagNodeId, tagClassNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.HAS_TYPE,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter tagClassIsSubclassOfTagClass( final BatchInserter batchInserter,
            final TagClassesBatchIndex tagClassesIndex ) throws FileNotFoundException
    {
        /*
        TagClass.id     TagClass.id
        211             239
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "tagclass_isSubclassOf_tagclass.csv" ),
                new CsvLineInserter()
                {
                    @Override
                    public Object[] transform( Object[] columnValues )
                    {
                        long subTagClassNodeId = tagClassesIndex.getIndex().get( "id",
                                Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                        long tagClassNodeId = tagClassesIndex.getIndex().get( "id",
                                Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                        return new Object[] { subTagClassNodeId, tagClassNodeId };
                    }

                    @Override
                    public void insert( Object[] columnValues )
                    {
                        batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                                Domain.Rel.IS_SUBCLASS_OF, EMPTY_MAP );
                    }
                } );
    }

    private static CsvFileInserter organisationBasedNearLocation( final BatchInserter batchInserter,
            final OrganisationsBatchIndex organisationsIndex, final LocationsBatchIndex locationsIndex )
            throws FileNotFoundException
    {
        /*
        Organisation.id     Location.id
        00                  301
         */
        return new CsvFileInserter( new File( RAW_DATA_DIR + "organisation_isLocatedIn_location.csv" ),
                new CsvLineInserter()
                {
                    @Override
                    public Object[] transform( Object[] columnValues )
                    {
                        long organisationNodeId = organisationsIndex.getIndex().get( "id",
                                Integer.parseInt( (String) columnValues[0] ) ).getSingle();
                        long locationNodeId = 0;
                        try
                        {
                            locationNodeId = locationsIndex.getIndex().get( "id",
                                    Integer.parseInt( (String) columnValues[1] ) ).getSingle();
                        }
                        catch ( Exception e )
                        {
                            /*
                             * TODO remove exception handling after generator fixed
                             * Location.id column contains ids that are not in location.csv
                             * eg. 301
                             */
                            logger.error( "Location not found: " + columnValues[1] );
                            return null;
                        }
                        return new Object[] { organisationNodeId, locationNodeId };
                    }

                    @Override
                    public void insert( Object[] columnValues )
                    {
                        // TODO remove when generator fixed
                        if ( columnValues == null ) return;
                        batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                                Domain.Rel.IS_LOCATED_IN, EMPTY_MAP );
                    }
                } );
    }

}
