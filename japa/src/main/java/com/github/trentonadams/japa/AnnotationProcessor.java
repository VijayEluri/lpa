/**
 * This file is part of the LDAP Persistence API (LPA).
 *
 * Copyright Trenton D. Adams <lpa at trentonadams daught ca>
 *
 * LPA is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * LPA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with LPA.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See the COPYING file for more information.
 */
package com.github.trentonadams.japa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes annotations all the way through a tree of Java classes, using
 * callbacks as the method of handling them.  See the {@link IAnnotationHandler}
 * for more information on the callbacks.
 * <p/>
 * Package processing occurs.  Classes are processed first.  Once the class
 * annotation processing has occurred, the constructor, field, and method
 * processing follow, in that order.
 * <p/>
 * Created :  21-Aug-2010 11:34:50 PM MST
 *
 * @author Trenton D. Adams
 */
@SuppressWarnings(
    {"PublicMethodNotExposedInInterface", "CollectionWithoutInitialCapacity"})
public class AnnotationProcessor
{
    protected List<IAnnotationHandler> handlers;

    /**
     * Initializes the handlers list.
     */
    public AnnotationProcessor()
    {
        handlers = new ArrayList<IAnnotationHandler>();
    }

    /**
     * Adds an annotation handler to the processing.
     *
     * @param annotationHandler annotation handler to make callbacks to.
     */
    public void addHandler(final IAnnotationHandler annotationHandler)
    {
        handlers.add(annotationHandler);
    }

    /**
     * Process the annotations using the handlers
     *
     * @return true
     */
    public void processAnnotations()
    {
        for (final IAnnotationHandler handler : handlers)
        {
            final Class annotatedClass = handler.getAnnotatedClass();
            if (processAnnotation(handler, annotatedClass))
            {
                handler.complete();
            }
        }
    }

    /**
     * Recursively process the class, and it's super classes, to the very root
     * of the object tree.  This is done by recursively grabbing the super class
     * until the result is null, in which case we're at the root.  Then, we call
     * the handler's {@link IAnnotationHandler#processAnnotation(Annotation,
     * Class)} method on the super class.  The method returns, and we call the
     * handler's method again, on the next class up the stack until we get to
     * the top.
     * <p/>
     * This method of recursive annotation processing allows for automatic
     * loading of appropriate data into different levels of the object
     * hierarchy.
     *
     * @param handler        the annotation handler
     * @param annotatedClass the annotated class to check for annotation
     */
    private boolean processAnnotation(final IAnnotationHandler handler,
        final Class annotatedClass)
    {
        final Class annotatedSuper;
        boolean processed = true;
        annotatedSuper = annotatedClass.getSuperclass();
        if (annotatedSuper != null)
        {   // recurse to the root of the tree first
            processed = processAnnotation(handler, annotatedSuper);
        }

        final Annotation[] classAnnotations = annotatedClass.getAnnotations();
        for (final Annotation classAnnotation : classAnnotations)
        {
            handler.processAnnotation(classAnnotation, annotatedClass);
        }

        handler.classAnnotationsComplete(annotatedClass);

        final Field[] fields = annotatedClass.getDeclaredFields();
        for (final Field field : fields)
        {
            final Annotation[] fieldAnnotations = field.getAnnotations();
            for (final Annotation fieldAnnotation : fieldAnnotations)
            {
                handler.processAnnotation(fieldAnnotation, annotatedClass,
                    field);
            }
        }

        return processed;
    }
}
