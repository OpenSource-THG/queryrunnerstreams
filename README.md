QueryRunner-Streams is the project containing the NamedParameterQueryRunner which has two developments on the standard QueryRunner.

The first, is the ability to stream results. Rather than applying a mapper to all rows, converting to a list, then to a stream, filtering, and then back to a list finally as happened in most database calls, you can now get the output of a SQL call as a stream, apply filters to it, before apply the mapper, than convert to a list once.
This results in more readable, easier to write code that is also potentially more performant.

The second development is that of having a NamedParameter version of a query runner, rather than having to keep track of question marks, you can simply name the parameters you are passing in, and replace the parameter array with a parameter map.
Additionally, there is the option of passing in lists, for example `SELECT * WHERE id IN (:ids[])` will allow you to pass in a Collection as the ids parameter.
