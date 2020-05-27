<%@ tag description="Training settings" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="settingsType" required="true" %>
<c:choose>
    <%-- General settings --%>
    <c:when test="${settingsType == 'general'}">
        <table class="compact">
            <tbody>
            <tr>
                <td><p>The number of folds (= the number of models) to train</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--n_folds" data-setting="--n_folds" type="number" value="5"/>
                        <label for="training--n_folds" data-type="int" data-error="Has to be integer">Default: 5 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Only train a single fold (= a single model)</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--single_fold" data-setting="--single_fold" type="number" value=""/>
                        <label for="training--single_fold" data-type="int" data-error="Has to be integer">Default: - (train all folds)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Number of models to train in parallel</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--max_parallel_models" data-setting="--max_parallel_models" type="number" />
                        <label for="training--max_parallel_models" data-type="int" data-error="Has to be integer">Default: -1 (Integer value) | Train all models in parallel</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                	<p>Whitelist of characters<br/>
                    <span class="userInfo">Will be kept even if they do not occur in the GT</span><br/>
                    <span class="userInfo">Example: ABCDEFGHIJ...012345</span>
                    </p>
                </td>
                <td>
                    <div class="input-field flex">
                        <!-- Temporary fix for faulty resizing of materialize-textarea -->
                        <input id="training--whitelist" data-setting="--whitelist" type="text" spellcheck="false"/>
                        <label for="training--whitelist">Default: A-Za-z0-9</label>
                        <a class="waves-effect waves-light dropdown-button btn whitelist-button" data-constrainWidth="false" href="#" data-activates="whitelist-select"><i class="material-icons">play_for_work</i></a>
                        <ul id="whitelist-select" class="dropdown-content"></ul>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Keep codec of the loaded model(s)</p></td>
                <td>
                    <p>
                        <input type="checkbox" class="filled-in" data-setting="--keep_loaded_codec" id="training--keep_loaded_codec" />
                        <label for="training--keep_loaded_codec"></label>
                    </p>
                </td>
            </tr>
            <tr>
                <td><p>Pretraining</p></td>
                <td>
                    <div class="input-field">
                        <i class="material-icons prefix">queue_play_next</i>
                        <select id="pretrainingType" name="pretrainingType" class="suffix ignoreParam">
                            <option value="from_scratch">Train all models from scratch</option>
                            <option value="single_model">Train all models based on one available model</option>
                            <option value="multiple_models">Train each model based on different available models</option>
                        </select>
                        <label></label>
                    </div>
                </td>
            </tr>
                <%-- START Will be used to generate dropdown elements for each model --%>
            <tr id="pretrainingDummyTr" style="display: none;" data-id="pretrainingTr">
                <td><p>Pretraining for model: <span></span></p></td>
                <td>
                    <div class="input-field">
                        <select id="pretrainingDummySelect" name="pretrainingDummySelect" data-id="pretrainingModelSelect" class="ignoreParam">
                            <option value="None">Train model from scratch</option>
                        </select>
                        <label></label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        Data augmentation
                        <br />
                        <span class="userInfo">Number of data augmentations per line</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--n_augmentations" data-setting="--n_augmentations" type="number"/>
                        <label for="training--n_augmentations" data-type="int" data-error="Has to be integer">Default: 0 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Skip retraining on real data only (faster but less accurate)</p></td>
                <td>
                    <p>
                        <input type="checkbox" class="filled-in" data-setting="--only_train_on_augmented" id="training--only_train_on_augmented" />
                        <label for="training--only_train_on_augmented"></label>
                    </p>
                </td>
            </tr>
                <%-- END Will be used to generate dropdown elements for each model --%>
            </tbody>
        </table>
    </c:when>
    <%-- Advanced settings --%>
    <c:when test="${settingsType == 'advanced'}">
        <table class="compact">
            <tbody>
            <tr>
                <td>
                    <p>
                        Early stopping
                        <br />
                        <span class="userInfo">The number of models that must be worse than the current best model to stop</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--early_stopping_nbest" data-setting="--early_stopping_nbest" type="number" value="5"/>
                        <label for="training--early_stopping_nbest" data-type="int" data-error="Has to be integer">Default: 10 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>
                    Early stopping frequency
                    <br />
                    <span class="userInfo">Number of training steps between the evaluation of the current model</span>
                </p></td>
                <td>
                    <div class="input-field">
                        <input id="training--early_stopping_frequency" data-setting="--early_stopping_frequency" type="number"/>
                        <label for="training--early_stopping_frequency" data-type="int" data-error="Has to be integer">Default: # GT lines / 2 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        The number of iterations for training
                        <br />
                        <span class="userInfo">If using early stopping, this is the maximum number of iterations</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--max_iters" data-setting="--max_iters" type="number" />
                        <label for="training--max_iters" data-type="int" data-error="Has to be integer">Default: 1000000 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        Training identifier
                        <br />
                        <span class="userInfo">A custom name can be used as identifier as well</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="trainingId" type="text" class="ignoreParam" />
                        <label for="trainingId">Default: Next unused incremented integer (0,1,2,...)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Hardware type</p></td>
                <td>
                    <div class="input-field">
                        <i class="material-icons prefix">perm_data_setting</i>
                        <select id="hardwareType" name="hardwareType" class="suffix ignoreParam">
                            <option value="CPU">CPU</option>
                            <option value="GPU">GPU</option>
                        </select>
                        <label></label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                	<p>Load data on the fly instead of preloading it<br>
                        <span class="userInfo">Reduces RAM usage, increases processing time</span>
					</p>
				</td>
                <td>
                    <p>
                        <input type="checkbox" class="filled-in" data-setting="--train_data_on_the_fly" id="training--train_data_on_the_fly" />
                        <label for="training--train_data_on_the_fly"></label>
                    </p>
                </td>
            </tr>
            <tr>
                <td><p>Console output frequency</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--display" data-setting="--display" type="number" value="50"/>
                        <label for="training--display" data-type="int" data-error="Has to be integer">Default: 1 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>The frequency how often to write checkpoints during training</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--checkpoint_frequency" data-setting="--checkpoint_frequency" type="number" />
                        <label for="training--checkpoint_frequency" data-type="int" data-error="Has to be integer">Default: 1000 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        Average this many iterations for:
                        <br />
                        <span class="userInfo">computing an average loss, label error rate and training time</span>

                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--stats_size" data-setting="--stats_size" type="number" />
                        <label for="training--stats_size" data-type="int" data-error="Has to be integer">Default: 1000 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>The default direction of text</p></td>
                <td>
                    <div class="input-field">
                        <select id="training--bidi_dir" data-setting="--bidi_dir" name="voter">
                            <option value="ltr">left to right</option>
                            <option value="rtl">right to left</option>
                        </select>
                        <label></label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>The batch size to use for training</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--batch_size" data-setting="--batch_size" type="number" step="1" value="3"/>
                        <label for="training--batch_size" data-type="int" data-error="Has to be integer">Default: 1 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Padding (left right) of the line</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--pad" data-setting="--pad" type="number" />
                        <label for="training--pad" data-type="int" data-error="Has to be integer">Default: 16 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>The line height</p></td>
                <td>
                    <div class="input-field">
                        <input id="training--line" data-setting="--line" type="number" />
                        <label for="training--line" data-type="int" data-error="Has to be integer">Default: 48 (Integer value)</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        The network structure
                        <br />
                        <span class="userInfo">Example: cnn=40:3x3,pool=2x2,cnn=60:3x3,pool=2x2,lstm=200,dropout=0.5</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--network" data-setting="--network" type="text" />
                        <label for="training--network" >Default: See example in description</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <p>
                        Seed for random operations
                        <br />
                        <span class="userInfo">If negative or zero a 'random' seed is used</span>
                    </p>
                </td>
                <td>
                    <div class="input-field">
                        <input id="training--seed" data-setting="--seed" type="number" />
                        <label for="training--seed" data-type="int" data-error="Has to be integer">Default: 0</label>
                    </div>
                </td>
            </tr>
            <tr>
                <td><p>Do no skip invalid gt, instead raise an exception</p></td>
                <td>
                    <p>
                        <input type="checkbox" class="filled-in" data-setting="--no_skip_invalid_gt" id="training--no_skip_invalid_gt" />
                        <label for="training--no_skip_invalid_gt"></label>
                    </p>
                </td>
            </tr>

				<tr>
					<td><p>Estimate skew angle for every region without one</p></td>
					<td>
						<p>
							<input type="checkbox" class="filled-in" data-setting="--estimate_skew" id="training--estimate_skew" checked="checked"/>
							<label for="training--estimate_skew"></label>
						</p>
					</td>
				</tr>
				<tr>
					<td><p>Maximum estimated skew of a region</p></td>
					<td>
						<div class="input-field">
							<input id="training--maxskew" data-setting="--maxskew" type="number" step="0.001"/>
							<label for="training--maxskew" data-type="float" data-error="Has to be a float">Default: 2.0</label>
						</div>
					</td>
				</tr>
				<tr>
					<td><p>Steps between 0 and +/-maxskew to estimate the possible skew of a region.</p></td>
					<td>
						<div class="input-field">
							<input id="training--skewsteps" data-setting="--skewsteps" type="number" step="1"/>
							<label for="training--skewsteps" data-type="int" data-error="Has to be a float">Default: 8</label>
						</div>
					</td>
				</tr>
            </tbody>
        </table>
    </c:when>
</c:choose>
